package com.zeroone.cloud.starter.oauth2.service;

import com.zeroone.cloud.oauth2.entity.SecurityUser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>
 * 描述：加载用户详细信息服务接口，用于对接不同数据库设计提供的外部接入接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public interface LoadUserDetailService {
    /**
     * 为加载用户详细信息
     * @param username 用户名
     * @param clientId 客户端ID
     * @return 用户详细信息对象
     * @throws UsernameNotFoundException 没有加载到用户抛出用户未找到异常
     */
    SecurityUser loadUserDetail(String username, String clientId) throws UsernameNotFoundException;
}
