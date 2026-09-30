package com.zeroone.cloud.starter.oauth2.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * 描述：Oauth2配置文件属性
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@ConfigurationProperties(prefix = "zo.cloud.starter.oauth2")
public class Oauth2Properties {
    /**
     * 是否启用
     */
    private boolean enabled = true;
    /**
     * 密钥库文件
     */
    private String jks = "jwt.jks";
    /**
     * 密钥库密码
     */
    private String jksPassword = "123456";
    /**
     * 密钥别名
     */
    private String keyAlias = "01star";
    /**
     * 密钥密码
     */
    private String keyPassword = "123456";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJks() {
        return jks;
    }

    public void setJks(String jks) {
        this.jks = jks;
    }

    public String getJksPassword() {
        return jksPassword;
    }

    public void setJksPassword(String jksPassword) {
        this.jksPassword = jksPassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
}
