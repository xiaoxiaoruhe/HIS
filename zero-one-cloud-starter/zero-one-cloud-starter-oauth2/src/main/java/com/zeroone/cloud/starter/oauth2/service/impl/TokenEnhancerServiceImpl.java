package com.zeroone.cloud.starter.oauth2.service.impl;

import com.zeroone.cloud.oauth2.entity.SecurityUser;
import com.zeroone.cloud.starter.oauth2.service.TokenEnhancerDataService;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 描述：Token增强实现，记录更多信息到Token里面
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Service
@ConditionalOnMissingBean(OAuth2TokenCustomizer.class)
public class TokenEnhancerServiceImpl implements OAuth2TokenCustomizer<JwtEncodingContext> {
    @Resource
    private TokenEnhancerDataService tokenEnhancerDataService;

    @Override
    public void customize(JwtEncodingContext context) {
        // 判断正在生成的令牌类型是否为访问令牌
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            Authentication principal = context.getPrincipal();
            context.getClaims().claims((claims) -> {
                // 添加角色到凭证负载中
                Set<String> roles = AuthorityUtils.authorityListToSet(principal.getAuthorities())
                        .stream()
                        .map(c -> c.replaceFirst("^ROLE_", ""))
                        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
                claims.put("roles", roles);
            });
            if (principal.getPrincipal() instanceof SecurityUser su) {
                // 调用外部扩展
                Map<String, Object> info = tokenEnhancerDataService.enhance(su);
                context.getClaims().claims(claims -> claims.putAll(info));
            }
        }
        // 判断正在生成的令牌类型是否为ID令牌
        else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            Authentication principal = context.getPrincipal();
            if (principal.getPrincipal() instanceof SecurityUser su) {
                // 调用外部扩展
                Map<String, Object> info = tokenEnhancerDataService.enhanceIdToken(su);
                context.getClaims().claims(claims -> claims.putAll(info));
            }
        }
    }
}