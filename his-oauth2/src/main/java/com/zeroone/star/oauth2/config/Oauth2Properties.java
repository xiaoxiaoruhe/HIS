package com.zeroone.star.oauth2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * <p>
 * 描述：应用模块属性配置类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "zo.oauth2")
public class Oauth2Properties {
    /**
     * 管理端ID
     */
    private String mgrId;
    /**
     * 用户端ID
     */
    private String userId;
    /**
     * 允许的跨域源
     */
    private List<String> allowedOrigins;
}
