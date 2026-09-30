package com.zeroone.star.project.config.filter.mcp;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * <p>
 * 描述：MCP 请求头过滤器，从入站 HTTP 请求中提取 Authorization 头，
 * 存入 RequestHeaderContext，供后续 MCP 工具调用时透传到下游服务
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 01star
 * @version 1.0.0
 */
@Component
@Order(1)
public class McpHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest httpRequest) {
                String authorization = httpRequest.getHeader("Authorization");
                if (StringUtils.hasText(authorization)) {
                    RequestHeaderContext.setAuthorization(authorization);
                }
            }
            chain.doFilter(request, response);
        } finally {
            // 请求结束后清除，防止线程池复用导致的内存泄漏
            RequestHeaderContext.clear();
        }
    }
}
