package com.zeroone.star.mg.config;

import com.zeroone.star.project.config.filter.mcp.RequestHeaderContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * <p>
 * 描述：MCP 客户端请求头配置，在 MCP Gateway 调用下游 MCP Server 时，
 * 自动将当前请求的 Authorization 头透传到工具调用请求中
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 01star
 * @version 1.0.0
 */
@Configuration
@ComponentScan({
        "com.zeroone.star.project.config.filter.mcp"
})
public class McpHeaderConfig {
    /**
     * 自定义 WebClient，
     * 在每次 HTTP 请求前从 RequestHeaderContext 读取 Authorization 并注入请求头
     */
    @Bean
    public WebClientCustomizer mcpWebClientCustomizer() {
        return builder -> builder.filter(this::addAuthorizationHeader);
    }

    @NotNull
    private Mono<ClientResponse> addAuthorizationHeader(
            ClientRequest request, ExchangeFunction next) {
        String authorization = RequestHeaderContext.getAuthorization();
        if (StringUtils.hasText(authorization)) {
            ClientRequest modifiedRequest = ClientRequest.from(request)
                    .header("Authorization", authorization)
                    .build();
            return next.exchange(modifiedRequest);
        }
        return next.exchange(request);
    }
}
