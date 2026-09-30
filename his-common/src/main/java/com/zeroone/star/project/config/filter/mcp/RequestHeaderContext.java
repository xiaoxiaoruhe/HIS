package com.zeroone.star.project.config.filter.mcp;

/**
 * <p>
 * 描述：请求头上下文，使用 ThreadLocal 存储当前请求的 Authorization 信息，
 * 供 MCP 客户端在调用下游 MCP Server 时透传请求头
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 01star
 * @version 1.0.0
 */
public class RequestHeaderContext {

    private static final ThreadLocal<String> AUTHORIZATION_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的 Authorization 头
     * @param authorization Bearer token
     */
    public static void setAuthorization(String authorization) {
        AUTHORIZATION_HOLDER.set(authorization);
    }

    /**
     * 获取当前线程的 Authorization 头
     * @return Bearer token，可能为 null
     */
    public static String getAuthorization() {
        return AUTHORIZATION_HOLDER.get();
    }

    /**
     * 清除当前线程的 Authorization 头，防止内存泄漏
     */
    public static void clear() {
        AUTHORIZATION_HOLDER.remove();
    }
}
