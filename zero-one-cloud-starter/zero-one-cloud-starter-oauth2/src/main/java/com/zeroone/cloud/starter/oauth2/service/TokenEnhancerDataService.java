package com.zeroone.cloud.starter.oauth2.service;

import com.zeroone.cloud.oauth2.entity.SecurityUser;

import java.util.Map;

/**
 * <p>
 * 描述：凭证增强数据处理服务接口，用于对接外部实现增强数据组装
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public interface TokenEnhancerDataService {
    /**
     * 增强Jwt负载数据
     * @param securityUser 安全用户数据对象
     * @return 返回增强后的Jwt数据字典
     */
    Map<String,Object> enhance(SecurityUser securityUser);

    /**
     * 增强ID Token负载数据
     * @param securityUser 安全用户数据对象
     * @return 返回增强后的ID Token数据字典
     */
    default Map<String,Object> enhanceIdToken(SecurityUser securityUser) {
        return Map.of();
    }
}
