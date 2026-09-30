package com.zeroone.cloud.starter.gateway.autoconfiguration;

import com.zeroone.cloud.starter.gateway.authorization.AuthorizationManager;
import com.zeroone.cloud.starter.gateway.handler.CommonSender;
import com.zeroone.cloud.starter.gateway.handler.RestfulAccessDeniedHandler;
import com.zeroone.cloud.starter.gateway.handler.RestfulAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 描述：支持组件组件定义
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Configuration
public class SupportCompConfig {
    @Bean
    public CommonSender commonSender() {
        return new CommonSender();
    }

    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }

    @Bean
    public RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint() {
        return new RestfulAuthenticationEntryPoint();
    }

    @Bean
    public AuthorizationManager authorizationManager() {
        return new AuthorizationManager();
    }
}
