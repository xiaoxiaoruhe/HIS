package com.zeroone.cloud.starter.gateway.service;

/**
 * <p>
 * 描述：响应数据（主要是异常数据）包装器，用于适配目标系统统一响应数据格式
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public interface ResponseDataWrapper {
    /**
     * 执行响应数据包装
     * @param code    结果码
     * @param message 结果码描述信息
     * @param data    其他数据
     * @return 包装后的响应数据
     */
    Object executeWrap(String code, String message, Object data);
}
