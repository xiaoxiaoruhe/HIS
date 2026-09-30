package com.zeroone.cloud.starter.gateway.filter;

import com.zeroone.cloud.starter.gateway.properties.GatewayProperties;
import jakarta.annotation.Resource;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * <p>
 * 描述：白名单路径处理
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class WhitePathFilter implements WebFilter {
    @Resource
    GatewayProperties properties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        // 白名单路径移除JWT请求头
        List<String> whiteUrls = properties.getWhitePaths();
        for (String whiteUrl : whiteUrls) {
            if (pathMatcher.match(whiteUrl, uri.getPath())) {
                request = exchange.getRequest().mutate().header("Authorization", "").build();
                exchange = exchange.mutate().request(request).build();
                return chain.filter(exchange);
            }
        }
        return chain.filter(exchange);
    }
}
