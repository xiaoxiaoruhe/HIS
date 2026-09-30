package com.zeroone.star.oauth2.config;

import com.zeroone.star.oauth2.service.impl.oauth2.RedisOauth2AuthorizationServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * <p>
 * 描述：Oauth2配置类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(Oauth2Properties.class)
public class Oauth2Config {
    @Resource
    Oauth2Properties properties;

    /**
     * 跨域配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.getAllowedOrigins());
        // 允许所有方法
        config.setAllowedMethods(List.of("*"));
        // 允许所有头
        config.setAllowedHeaders(List.of("*"));
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 暴露所有头
        config.setExposedHeaders(List.of("*"));
        // 预检缓存时间
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 创建授权服务, 将授权信息存储到Redis中
     * @param redisTemplate RedisTemplate 用于操作Redis的模板对象
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisOauth2AuthorizationServiceImpl(redisTemplate);
    }
}
