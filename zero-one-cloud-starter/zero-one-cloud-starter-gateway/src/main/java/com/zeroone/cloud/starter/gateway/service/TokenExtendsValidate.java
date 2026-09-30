package com.zeroone.cloud.starter.gateway.service;

/**
 * <p>
 * 描述：凭证扩展校验接口，用于适配目标系统的扩展校验机制，如：验证凭证是否注销
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public interface TokenExtendsValidate {
    /**
     * 是否已经注销
     * @param token 待验证凭证
     * @return 如果已注销，返回true，否则返回false
     */
    boolean isLogout(String token);
}
