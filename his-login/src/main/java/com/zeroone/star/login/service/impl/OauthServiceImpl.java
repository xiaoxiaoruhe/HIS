package com.zeroone.star.login.service.impl;

import com.zeroone.star.login.service.OauthService;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * <p>
 * 描述：授权服务降级实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@AllArgsConstructor
public class OauthServiceImpl implements OauthService {
    private Throwable throwable;

    /**
     * 通用错误处理
     * @return 错误信息
     */
    private Map<String, Object> commonError() {
        if (throwable.getMessage() != null) {
            return Map.of(OauthService.ERROR_KEY, throwable.getMessage());
        } else {
            return Map.of(OauthService.ERROR_KEY, throwable.getClass().toGenericString());
        }
    }

    @Override
    public Map<String, Object> postAccessToken(Map<String, String> parameters) {
        return commonError();
    }

    @Override
    public Map<String, Object> revokeToken(Map<String, String> parameters) {
        return commonError();
    }
}
