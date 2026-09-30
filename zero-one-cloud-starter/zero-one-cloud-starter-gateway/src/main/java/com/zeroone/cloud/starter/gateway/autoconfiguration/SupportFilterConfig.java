package com.zeroone.cloud.starter.gateway.autoconfiguration;

import com.zeroone.cloud.starter.gateway.filter.AuthGlobalFilter;
import com.zeroone.cloud.starter.gateway.filter.WhitePathFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 描述：支持过滤器定义
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
public class SupportFilterConfig {
    @Bean
    public WhitePathFilter whitePathFilter() {
        return new WhitePathFilter();
    }

    @Bean
    public AuthGlobalFilter authGlobalFilter() {
        return new AuthGlobalFilter();
    }
}
