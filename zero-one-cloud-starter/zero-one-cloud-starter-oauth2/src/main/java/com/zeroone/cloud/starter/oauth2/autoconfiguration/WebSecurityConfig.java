package com.zeroone.cloud.starter.oauth2.autoconfiguration;

import com.zeroone.cloud.starter.oauth2.service.TokenGranterStrategy;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 描述：SpringSecurity配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    /**
     * 收集容器中所有的grant扩展策略
     */
    @Autowired(required = false)
    private List<TokenGranterStrategy> customStrategies = new ArrayList<>();
    @Resource
    private OAuth2AuthorizationService authorizationService;
    @Resource
    private OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    /**
     * 统一处理退出逻辑，销毁session与跳转到登录回调页面
     * @param request  请求
     * @param response 响应
     * @throws IOException IO异常
     */
    private void commonOidcLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 手动销毁服务器端的Session
        // 否则浏览器带着JSESSIONID回来又是登录状态
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 清除SecurityContext
        SecurityContextHolder.clearContext();

        // 跳转回前端的退出成功地址
        String postLogoutRedirectUri = request.getParameter("post_logout_redirect_uri");
        new DefaultRedirectStrategy().sendRedirect(request, response,
                StringUtils.hasText(postLogoutRedirectUri) ? postLogoutRedirectUri : "/login");
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // 初始化授权服务器配置器
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
                // 只匹配OAuth2核心端点
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .cors(Customizer.withDefaults())
                .csrf(Customizer.withDefaults())
                // 应用授权服务器配置
                .with(authorizationServerConfigurer, (authorizationServer) -> {
                    // 开启OIDC支持
                    //authorizationServer.oidc(Customizer.withDefaults());
                    authorizationServer.oidc(oidc -> oidc
                            .logoutEndpoint(logoutEndpoint -> logoutEndpoint
                                    .logoutResponseHandler((request, response, authentication) -> {
                                        // 此时认证已经成功（默认Provider已经跑完）
                                        if (authentication instanceof OidcLogoutAuthenticationToken logoutToken) {
                                            // 拿到IDToken字符串
                                            String idTokenValue = logoutToken.getIdTokenHint();
                                            if (StringUtils.hasText(idTokenValue)) {
                                                // 查出并删除Redis凭证
                                                OAuth2Authorization auth = authorizationService.findByToken(idTokenValue, new OAuth2TokenType("id_token"));
                                                if (auth != null) {
                                                    authorizationService.remove(auth);
                                                }
                                            }
                                        }
                                        // 退出登录逻辑
                                        commonOidcLogout(request, response);
                                    })
                                    .errorResponseHandler((request, response, exception) -> {
                                        // 此时说明id_token_hint校验失败
                                        // 虽然没法精准删除凭证，但我们必须让用户退出登录
                                        commonOidcLogout(request, response);
                                    })
                            )
                    );
                    // 遍历所有自定义策略，注入到SAS中
                    if (!customStrategies.isEmpty()) {
                        System.out.println("===============Cus Granter Start===============");
                        authorizationServer.tokenEndpoint(tokenEndpoint -> customStrategies.forEach(strategy -> {
                                    System.out.println("Inject " + strategy.getGrantType() + " customer grant mode.");
                                    // 注册转换器：负责解析请求参数
                                    tokenEndpoint.accessTokenRequestConverter(strategy.getConverter());
                                    // 注册提供者：负责执行认证逻辑
                                    tokenEndpoint.authenticationProvider(strategy.getProvider(authorizationService, tokenGenerator));
                                })
                        );
                        System.out.println("===============Cus Granter End=================");
                    }
                })
                // 配置请求授权规则
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                // 配置异常处理
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                // 1. 核心端点认证失败时，直接返回401状态码，不重定向
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                // 2. 匹配非HTML请求
                                new NegatedRequestMatcher(new MediaTypeRequestMatcher(MediaType.TEXT_HTML))
                        )
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // 配置OAuth2资源服务器
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain openApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 开放接口放行，用于接入后扩展一些功能接口
                .securityMatcher("/open/**")
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
                // 禁用CSRF（API无状态，不需要）
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // 在SecurityConfig中过滤掉error路径的缓存
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        // 只要请求路径包含"error"，就返回false (不缓存)
        requestCache.setRequestMatcher(request -> {
            String url = request.getRequestURI();
            // 排除error路径以及一些可能导致干扰的静态资源
            return !url.contains("/error") && !url.contains("/favicon.ico");
        });
        http.requestCache(cache -> cache.requestCache(requestCache));
        http
                .cors(Customizer.withDefaults())
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests((authorize) -> authorize
                        // 放行所有静态资源
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // 登录界面
                        .requestMatchers("/login", "/login?error", "/login?logout").permitAll()
                        // 其他所有请求需认证
                        .anyRequest().authenticated())
                // 自定义登出设置
                .logout(LogoutConfigurer::permitAll)
                // 自定义登录设置
                .formLogin(form -> form.
                        loginPage("/login")
                        .loginProcessingUrl("/login")
                        .permitAll()
                );
        return http.build();
    }
}