package com.zeroone.cloud.starter.oauth2.service.grant;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 描述：定义一个抽象认证Provider
 * 用于处理SAS的核心逻辑：校验客户端权限、生成Token、构建上下文
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public abstract class AbstractCustomAuthenticationProvider implements AuthenticationProvider {
    /**
     * 认证服务
     */
    private final OAuth2AuthorizationService authorizationService;
    /**
     * Token生成器
     */
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    /**
     * 构造初始化
     * @param authorizationService 认证服务
     * @param tokenGenerator       Token生成器
     */
    protected AbstractCustomAuthenticationProvider(OAuth2AuthorizationService authorizationService, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取自定义认证Token
        CustomGrantAuthenticationToken customToken = (CustomGrantAuthenticationToken) authentication;

        // 1. 确保客户端已认证（SAS内部逻辑）
        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(customToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        // 2. 验证该客户端是否允许此授权模式
        if (!registeredClient.getAuthorizationGrantTypes().contains(customToken.getGrantType())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        // 3. 执行业务校验 (由子类实现：如校验验证码、查数据库)
        Authentication userPrincipal = authenticateUser(customToken, registeredClient);

        // 4. 确定Scope权限，默认给全部，或根据请求过滤
        Set<String> authorizedScopes = registeredClient.getScopes();

        // 5. 构建Token生成上下文
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                // 放入认证后的用户信息
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(customToken.getGrantType())
                .authorizationGrant(customToken);

        // 6. 生成AccessToken
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the access token.", null);
            throw new OAuth2AuthenticationException(error);
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());

        // 7. 初始化OAuth2Authorization
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userPrincipal.getName())
                .authorizationGrantType(customToken.getGrantType())
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userPrincipal);
        if (generatedAccessToken instanceof ClaimAccessor claimAccessor) {
            authorizationBuilder.token(accessToken, (metadata) -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        // 8. 生成RefreshToken
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = tokenGenerator.generate(tokenContext);
            if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the refresh token.", null);
                throw new OAuth2AuthenticationException(error);
            }
            refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
            // 将RefreshToken放入持久化构建器
            authorizationBuilder.refreshToken(refreshToken);
        }

        // 9. 生成ID Token
        OidcIdToken idToken = null;
        if (authorizedScopes.contains(OidcScopes.OPENID)) {
            tokenContext = tokenContextBuilder.tokenType(new OAuth2TokenType(OidcParameterNames.ID_TOKEN)).build();
            OAuth2Token generatedIdToken = tokenGenerator.generate(tokenContext);
            if (generatedIdToken instanceof Jwt jidToken) {
                idToken = new OidcIdToken(
                        generatedIdToken.getTokenValue(),
                        generatedIdToken.getIssuedAt(),
                        generatedIdToken.getExpiresAt(),
                        jidToken.getClaims()
                );
                authorizationBuilder.token(idToken, (metadata) -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, jidToken.getClaims()));
            }
        }

        // 10. 保存OAuth2Authorization
        authorizationService.save(authorizationBuilder.build());

        // 11. 构建自定义响应参数 (用于存放 ID Token)
        Map<String, Object> additionalParameters = Collections.emptyMap();
        if (idToken != null) {
            additionalParameters = new HashMap<>();
            additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
        }

        // 12. 构建最终的认证结果 (包含AccessToken和可选的RefreshToken)
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    /**
     * 抽象方法：进行具体的业务账号密码或验证码校验
     * @param token            认证凭证实现对象
     * @param registeredClient 客户端注册信息
     * @return 返回认证后的 UserPrincipal (通常包含UserDetails)
     */
    protected abstract Authentication authenticateUser(CustomGrantAuthenticationToken token, RegisteredClient registeredClient);

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 获取已认证的客户端信息
     * @param authentication 认证信息
     * @return 返回已认证的客户端信息
     */
    private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken clientPrincipal && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}
