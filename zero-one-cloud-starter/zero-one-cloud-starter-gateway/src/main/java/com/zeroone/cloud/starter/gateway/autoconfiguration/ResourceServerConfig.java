package com.zeroone.cloud.starter.gateway.autoconfiguration;

import com.zeroone.cloud.starter.gateway.authorization.AuthorizationManager;
import com.zeroone.cloud.starter.gateway.filter.WhitePathFilter;
import com.zeroone.cloud.starter.gateway.handler.RestfulAccessDeniedHandler;
import com.zeroone.cloud.starter.gateway.handler.RestfulAuthenticationEntryPoint;
import com.zeroone.cloud.starter.gateway.properties.GatewayProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;

/**
 * <p>
 * 描述：资源服务器配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Configuration
@ConditionalOnClass(GatewayProperties.class)
@EnableConfigurationProperties(GatewayProperties.class)
@Import({SupportCompConfig.class, SupportFilterConfig.class, CorsConfig.class})
@EnableWebFluxSecurity
public class ResourceServerConfig {
    private final AuthorizationManager authorizationManager;
    private final GatewayProperties gatewayProperties;
    private final RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    private final RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;
    private final WhitePathFilter whitePathFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public ResourceServerConfig(AuthorizationManager authorizationManager,
                                GatewayProperties gatewayProperties,
                                RestfulAccessDeniedHandler restfulAccessDeniedHandler,
                                RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint,
                                WhitePathFilter whitePathFilter,
                                CorsConfigurationSource corsConfigurationSource) {
        this.authorizationManager = authorizationManager;
        this.gatewayProperties = gatewayProperties;
        this.restfulAccessDeniedHandler = restfulAccessDeniedHandler;
        this.restfulAuthenticationEntryPoint = restfulAuthenticationEntryPoint;
        this.whitePathFilter = whitePathFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        //禁用csrf
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        //跨域支持
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));
        //JWT配置
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(restfulAuthenticationEntryPoint)
        );
        //对白名单路径，直接移除JWT请求头
        http.addFilterBefore(whitePathFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        //请求授权
        http.authorizeExchange(exchanges -> exchanges
                        //白名单配置
                        .pathMatchers(gatewayProperties.getWhitePaths().toArray(new String[0])).permitAll()
                        //内部请求白名单
                        .pathMatchers(gatewayProperties.getInnerPaths().toArray(new String[0])).permitAll()
                        //OPTIONS预检请求直接放行
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        //鉴权管理器配置
                        .anyExchange().access(authorizationManager)
                )
                //异常处理
                .exceptionHandling(exceptions -> exceptions
                        //处理未授权
                        .accessDeniedHandler(restfulAccessDeniedHandler)
                        //处理未认证
                        .authenticationEntryPoint(restfulAuthenticationEntryPoint)
                );
        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(gatewayProperties.getAuthorityPrefix());
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(gatewayProperties.getAuthorityClaimName());
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
