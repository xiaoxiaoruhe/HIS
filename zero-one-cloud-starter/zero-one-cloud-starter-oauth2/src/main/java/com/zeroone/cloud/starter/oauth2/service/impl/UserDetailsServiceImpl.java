package com.zeroone.cloud.starter.oauth2.service.impl;

import com.zeroone.cloud.starter.oauth2.service.LoadUserDetailService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 描述：用户详细信息加载服务实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Service
@ConditionalOnMissingBean(UserDetailsService.class)
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    HttpServletRequest request;
    @Resource
    LoadUserDetailService loadService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从请求中获取client_id
        String clientId = request.getParameter("client_id");
        // 从Session中获取client_id
        if (!StringUtils.hasText(clientId)) {
            Object tmp = request.getSession().getAttribute("client_id");
            if (tmp != null) {
                clientId = tmp.toString();
            }
        }
        // 调用外部接口获取用户信息
        if (clientId != null) {
            return loadService.loadUserDetail(username, clientId);
        }
        throw new UsernameNotFoundException("登录客户端ID错误");
    }
}
