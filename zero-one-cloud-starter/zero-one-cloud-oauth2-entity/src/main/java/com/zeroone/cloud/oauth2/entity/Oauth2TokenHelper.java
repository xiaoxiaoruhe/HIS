package com.zeroone.cloud.oauth2.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 描述：Oauth2获取Token接口参数帮助类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class Oauth2TokenHelper {
    /**
     * 构建获取Token接口参数
     * @param grantType            授权模式
     * @param clientId             客户端ID
     * @param clientSecret         客户端密钥
     * @param additionalParameters 附加的参数
     * @return 获取Token接口参数
     */
    public static Map<String, String> buildParams(String grantType, String clientId, String clientSecret, Map<String, String> additionalParameters) {
        Map<String, String> params = new HashMap<>(Map.of(
                "grant_type", grantType,
                "client_id", clientId,
                "client_secret", clientSecret
        ));
        if (additionalParameters != null && !additionalParameters.isEmpty()) {
            params.putAll(additionalParameters);
        }
        return params;
    }

    /**
     * 构建无附加参数，获取Token接口参数
     * @param grantType    授权模式
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @return 获取Token接口参数
     */
    public static Map<String, String> buildParams(String grantType, String clientId, String clientSecret) {
        return buildParams(grantType, clientId, clientSecret, null);
    }

    /**
     * 构建无权限校验，获取Token接口参数
     * @param grantType            授权模式
     * @param clientId             客户端ID
     * @param additionalParameters 额外的参数
     * @return 获取Token接口参数
     */
    public static Map<String, String> buildParams(String grantType, String clientId, Map<String, String> additionalParameters) {
        Map<String, String> params = new HashMap<>(Map.of(
                "grant_type", grantType,
                "client_id", clientId
        ));
        if (additionalParameters != null && !additionalParameters.isEmpty()) {
            params.putAll(additionalParameters);
        }
        return params;
    }

    /**
     * 构建无附加参数，无权限校验，获取Token接口参数
     * @param grantType 授权模式
     * @param clientId  客户端ID
     * @return 获取Token接口参数
     */
    public static Map<String, String> buildParams(String grantType, String clientId) {
        return buildParams(grantType, clientId, Collections.emptyMap());
    }

    /**
     * 构建刷新凭证模式，获取Token接口参数
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken 刷新令牌
     * @return 刷新模式获取Token接口参数
     */
    public static Map<String, String> buildParamsForRefreshTokenGrant(String clientId, String clientSecret, String refreshToken) {
        return buildParams("refresh_token", clientId, clientSecret, Map.of("refresh_token", refreshToken));
    }

    /**
     * 构建密码模式，获取Token接口参数,
     * 注意：密码模式已弃用
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @param username     用户名
     * @param password     密码
     * @return 返回Token接口参数
     */
    @Deprecated
    public static Map<String, String> buildParamsForPasswordGrant(String clientId, String clientSecret, String username, String password) {
        return buildParams("password", clientId, clientSecret, Map.of("username", username, "password", password));
    }

    /**
     * 构建注销凭证接口参数
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @param token        令牌
     * @return 获取Token接口参数
     */
    public static Map<String, String> buildParamsForRevoke(String clientId, String clientSecret, String token) {
        return Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "token", token
        );
    }
}