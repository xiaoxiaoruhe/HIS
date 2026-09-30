package com.zeroone.star.login.service;

import com.zeroone.star.login.fallback.OauthServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * <p>
 * 描述：授权声明式服务接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@FeignClient(value = "${sn.auth}", fallbackFactory = OauthServiceFallbackFactory.class)
public interface OauthService {
    /**
     * 记录错误信息的key
     */
    String ERROR_KEY = "error";

    /**
     * 获取认证令牌
     * @param parameters 参数列表
     * @return 结果数据
     */
    @PostMapping(value = "/oauth2/token", consumes = "application/x-www-form-urlencoded")
    Map<String, Object> postAccessToken(@RequestBody Map<String, String> parameters);

    /**
     * 撤销认证令牌
     * @param parameters 参数列表
     * @return 结果数据
     */
    @PostMapping(value = "/oauth2/revoke", consumes = "application/x-www-form-urlencoded")
    Map<String, Object> revokeToken(@RequestBody Map<String, String> parameters);
}
