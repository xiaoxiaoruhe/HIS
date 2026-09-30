package com.zeroone.cloud.starter.oauth2.service.grant;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * 描述：定义一个通用的Authentication实现，用于承载请求参数和用户信息
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class CustomGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    /**
     * 附加参数存储容器，用于存储请求参数
     */
    private final Map<String, Object> additionalParameters;

    /**
     * 构造初始化
     * @param grantType            授权模式类型名
     * @param clientPrincipal      客户端认证信息
     * @param additionalParameters 附加参数
     */
    public CustomGrantAuthenticationToken(String grantType,
                                          Authentication clientPrincipal,
                                          @Nullable Map<String, Object> additionalParameters) {
        super(new AuthorizationGrantType(grantType), clientPrincipal, additionalParameters);
        this.additionalParameters = Collections.unmodifiableMap(
                additionalParameters != null ? additionalParameters : Collections.emptyMap());
    }

    @Override
    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }
}
