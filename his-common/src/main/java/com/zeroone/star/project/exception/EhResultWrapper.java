package com.zeroone.star.project.exception;

import com.zeroone.star.project.enums.ResultStatus;

/**
 * <p>
 * 描述：异常拦截响应数据包装器
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public interface EhResultWrapper {
    /**
     * 响应数据包装
     * @param message 错误信息
     * @param status  错误码
     * @return 响应数据
     */
    Object getResult(String message, ResultStatus status);
}