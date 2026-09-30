package com.zeroone.cloud.starter.oauth2.service;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * <p>
 * 描述：扩展TokenGranter策略接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public interface TokenGranterStrategy {
    /**
     * 获取授权模式类型名称，名称建议小写字母多个单调用_连接，如sms_code
     * @return 返回授权模式类型名称
     */
    String getGrantType();

    /**
     * 获取授权模式对应的转换器
     * @return 将HttpServletRequest转换为Authentication对象的转换器
     */
    AuthenticationConverter getConverter();

    /**
     * 获取授权模式对应的Provider
     * @param authorizationService 认证服务对象
     * @param tokenGenerator       Token生成器对象
     * @return 处理具体认证逻辑的Provider
     */
    AuthenticationProvider getProvider(OAuth2AuthorizationService authorizationService, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator);
}
