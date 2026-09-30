package com.zeroone.cloud.starter.oauth2.service.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 描述：自定义认证转换器, 根据getGrantType()自动做前置检查，让子类Converter只需关注如何获取参数
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public abstract class AbstractCustomAuthenticationConverter<T extends Authentication> implements AuthenticationConverter {
    /**
     * 授权模式类型名称
     */
    protected final String grantType;

    /**
     * 构造方法
     * @param grantType 授权模式类型名称
     */
    protected AbstractCustomAuthenticationConverter(String grantType) {
        this.grantType = grantType;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 1. 统一校验 grant_type
        String requestGrantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!this.grantType.equals(requestGrantType)) {
            return null;
        }

        // 2. 获取当前已认证的客户端信息 (Client Principal)
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 3. 提取所有请求参数
        Map<String, Object> additionalParameters = getParameters(request);

        // 4. 调用子类模板方法，创建具体的Token
        return createAuthentication(clientPrincipal, additionalParameters);
    }

    /**
     * 创建认证信息，处理如何封装Token
     * @param clientPrincipal      认证信息
     * @param additionalParameters 请求参数
     * @return 认证信息
     */
    protected abstract T createAuthentication(Authentication clientPrincipal, Map<String, Object> additionalParameters);

    /**
     * 将Request参数转为Map
     * @param request 请求对象
     * @return 参数Map
     */
    private Map<String, Object> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> parameters = new HashMap<>(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            if (values.length > 0) {
                parameters.put(key, values[0]);
            }
        });
        return parameters;
    }
}
