package com.zeroone.cloud.starter.gateway.handler;

import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>
 * 描述：没有权限访问时下发消息
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class RestfulAccessDeniedHandler implements ServerAccessDeniedHandler {
    @Resource
    CommonSender sender;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return sender.sender(exchange, String.valueOf(HttpStatus.FORBIDDEN.value()), "没有相关权限", denied.getMessage());
    }
}
