package com.zeroone.cloud.starter.gateway.autoconfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * <p>
 * 描述：跨域配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有源
        config.setAllowedOrigins(List.of("*"));
        // 允许所有方法
        config.setAllowedMethods(List.of("*"));
        // 允许所有头
        config.setAllowedHeaders(List.of("*"));
        // 不允许携带凭证（当允许所有源时必须为false）
        config.setAllowCredentials(false);
        // 暴露所有头
        config.setExposedHeaders(List.of("*"));
        // 预检缓存时间
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
