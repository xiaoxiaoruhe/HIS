package com.zeroone.cloud.starter.gateway.filter;

import com.nimbusds.jose.JWSObject;
import com.zeroone.cloud.starter.gateway.handler.CommonSender;
import com.zeroone.cloud.starter.gateway.service.TokenExtendsValidate;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.yaml.snakeyaml.util.UriEncoder;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * <p>
 * 描述：将登录用户的JWT转化成用户信息的全局过滤器
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Resource
    TokenExtendsValidate tev;

    @Resource
    CommonSender sender;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取凭证
        String token = getToken(exchange);
        // 凭证为空直接放行
        if (!StringUtils.hasText(token)) {
            return chain.filter(exchange);
        }
        String realToken = token.replace("Bearer ", "");
        // 判断凭证是否注销
        if (tev.isLogout(realToken)) {
            return sender.sender(exchange, String.valueOf(HttpStatus.UNAUTHORIZED.value()), "暂未登录或TOKEN已经过期", "用户凭证已注销");
        }
        try {
            // 从token中解析用户信息并设置到Header中去
            JWSObject jwsObject = JWSObject.parse(realToken);
            String userStr = jwsObject.getPayload().toString();
            ServerHttpRequest request = exchange.getRequest().mutate().header("user", UriEncoder.encode(userStr)).build();
            exchange = exchange.mutate().request(request).build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return chain.filter(exchange);
    }

    /**
     * 获取token
     * @param exchange exchange对象
     * @return 返回获取到的凭证，否则返回null
     */
    private String getToken(ServerWebExchange exchange) {
        // 获取请求头中JWT令牌
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(token)) {
            return token;
        }
        // 如果是websocket请求
        if ("websocket".equalsIgnoreCase(exchange.getRequest().getHeaders().getUpgrade())) {
            // 获取查询参数中的token
            token = exchange.getRequest().getQueryParams().getFirst("token");
            // 获取协议作为token值
            if (!StringUtils.hasText(token)) {
                token = exchange.getRequest().getHeaders().getFirst("Sec-WebSocket-Protocol");
                // 做一个满足jwt格式长度判断
                if (StringUtils.hasText(token) && token.length() > 100) {
                    return token;
                }
            } else {
                return token;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

