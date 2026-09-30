package com.zeroone.cloud.starter.gateway.handler;

import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>
 * 描述：没有登录或token过期时下发消息
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class RestfulAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    @Resource
    CommonSender sender;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        return sender.sender(exchange, String.valueOf(HttpStatus.UNAUTHORIZED.value()), "暂未登录或TOKEN已经过期", e.getMessage());
    }
}
