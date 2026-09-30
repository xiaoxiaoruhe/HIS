package com.zeroone.cloud.fastdfs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * 描述：fastdfs配置属性
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@ConfigurationProperties(prefix = "fastdfs")
public class FastdfsProperties {
    /**
     * 连接超时时间（s）
     */
    private String connectTimeout = "5";
    /**
     * 网络超时时间（读写数据的时间s）
     */
    private String networkTimeout = "30";
    /**
     * 字符集编码
     */
    private String charset = "UTF-8";
    /**
     * 是否使用Token
     */
    private String httpAntiStealToken = "false";
    /**
     * Token加密密钥
     */
    private String httpSecretKey = "FastDFS1234567890";
    /**
     * 当需要通过Tracker获取Storage地址或进行HTTP访问时，使用的端口
     */
    private String httpTrackerHttpPort = "80";
    /**
     * 跟踪器集群的地址列表，多个使用分号隔开
     */
    private String trackerServers = "";
    /**
     * 连接池的连接对象最大个数
     */
    private String connectionPoolMaxTotal = "18";
    /**
     * 连接池的最大空闲对象个数
     */
    private String connectionPoolMaxIdle = "18";
    /**
     * 连接池的最小空闲对象个数
     */
    private String connectionPoolMinIdle = "2";
    /**
     * Nginx服务器集群的IP地址列表，多个使用分号隔开
     */
    private String nginxServers = "";

    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setNetworkTimeout(String networkTimeout) {
        this.networkTimeout = networkTimeout;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setHttpAntiStealToken(String httpAntiStealToken) {
        this.httpAntiStealToken = httpAntiStealToken;
    }

    public void setHttpSecretKey(String httpSecretKey) {
        this.httpSecretKey = httpSecretKey;
    }

    public void setHttpTrackerHttpPort(String httpTrackerHttpPort) {
        this.httpTrackerHttpPort = httpTrackerHttpPort;
    }

    public void setTrackerServers(String trackerServers) {
        this.trackerServers = trackerServers;
    }

    public void setConnectionPoolMaxTotal(String connectionPoolMaxTotal) {
        this.connectionPoolMaxTotal = connectionPoolMaxTotal;
    }

    public void setConnectionPoolMaxIdle(String connectionPoolMaxIdle) {
        this.connectionPoolMaxIdle = connectionPoolMaxIdle;
    }

    public void setConnectionPoolMinIdle(String connectionPoolMinIdle) {
        this.connectionPoolMinIdle = connectionPoolMinIdle;
    }

    public void setNginxServers(String nginxServers) {
        this.nginxServers = nginxServers;
    }

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public String getNetworkTimeout() {
        return networkTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public String getHttpAntiStealToken() {
        return httpAntiStealToken;
    }

    public String getHttpSecretKey() {
        return httpSecretKey;
    }

    public String getHttpTrackerHttpPort() {
        return httpTrackerHttpPort;
    }

    public String getTrackerServers() {
        return trackerServers;
    }

    public String getConnectionPoolMaxTotal() {
        return connectionPoolMaxTotal;
    }

    public String getConnectionPoolMaxIdle() {
        return connectionPoolMaxIdle;
    }

    public String getConnectionPoolMinIdle() {
        return connectionPoolMinIdle;
    }

    public String getNginxServers() {
        return nginxServers;
    }
}