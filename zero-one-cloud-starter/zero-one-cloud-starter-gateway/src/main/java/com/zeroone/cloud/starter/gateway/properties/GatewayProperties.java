package com.zeroone.cloud.starter.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * <p>
 * 描述：网关白名单配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@ConfigurationProperties(prefix = "zo.cloud.starter.gateway")
@RefreshScope
public class GatewayProperties {
    /**
     * 请求白名单
     */
    private List<String> whitePaths;
    /**
     * 内部请求白名单，用于配置需要使用token但不需要权限验证的请求
     */
    private List<String> innerPaths;
    /**
     * JWT存储权限前缀
     */
    private String authorityPrefix = "ROLE_";
    /**
     * JWT存储权限属性
     */
    private String authorityClaimName = "authorities";
    /**
     * 是否开启权限认证
     */
    private boolean openAuthorization = true;

    public List<String> getWhitePaths() {
        return whitePaths;
    }

    public void setWhitePaths(List<String> whitePaths) {
        this.whitePaths = whitePaths;
    }

    public String getAuthorityPrefix() {
        return authorityPrefix;
    }

    public void setAuthorityPrefix(String authorityPrefix) {
        this.authorityPrefix = authorityPrefix;
    }

    public String getAuthorityClaimName() {
        return authorityClaimName;
    }

    public void setAuthorityClaimName(String authorityClaimName) {
        this.authorityClaimName = authorityClaimName;
    }

    public boolean isOpenAuthorization() {
        return openAuthorization;
    }

    public void setOpenAuthorization(boolean openAuthorization) {
        this.openAuthorization = openAuthorization;
    }

    public List<String> getInnerPaths() {
        return innerPaths;
    }

    public void setInnerPaths(List<String> innerPaths) {
        this.innerPaths = innerPaths;
    }
}
