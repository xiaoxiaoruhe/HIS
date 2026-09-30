package com.zeroone.cloud.fastdfs.service;

import com.zeroone.cloud.fastdfs.factory.StorageClientFactory;
import com.zeroone.cloud.fastdfs.properties.FastdfsProperties;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

import static java.lang.Math.abs;

/**
 * <p>
 * 描述：
 * 为SpringBoot构建的fastDFS场景启动器
 * 封装fastDFS中的方法，实现带有Token的方法
 * 加入Nginx负载均衡策略
 * 可以直接调用方法，直接返回文件请求地址
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class FastdfsClientService {
    /**
     * SpringBoot加载的配置文件
     */
    private final FastdfsProperties dfsProp;
    /**
     * 连接池
     */
    private GenericObjectPool<StorageClient> pool;
    /**
     * Nginx服务器列表
     */
    private String[] nginxServers;
    /**
     * 日志记录器
     */
    private final Logger logger;

    /**
     * 构造方法
     * @param dfsProp SpringBoot加载的配置文件
     */
    public FastdfsClientService(FastdfsProperties dfsProp) throws Exception {
        this.dfsProp = dfsProp;
        this.logger = LoggerFactory.getLogger(getClass());
        init();
        create();
        info();
    }

    /**
     * 初始化全局客户端
     */
    private void init() throws Exception {
        Properties prop = new Properties();
        this.logger.info("FastDFS: reading config file...");
        this.logger.info("FastDFS: fastdfs.connect_timeout_in_seconds=" + this.dfsProp.getConnectTimeout());
        this.logger.info("FastDFS: fastdfs.network_timeout_in_seconds=" + this.dfsProp.getNetworkTimeout());
        this.logger.info("FastDFS: fastdfs.charset=" + this.dfsProp.getCharset());
        this.logger.info("FastDFS: fastdfs.http_anti_steal_token=" + this.dfsProp.getHttpAntiStealToken());
        this.logger.info("FastDFS: fastdfs.http_secret_key=" + this.dfsProp.getHttpSecretKey());
        this.logger.info("FastDFS: fastdfs.http_tracker_http_port=" + this.dfsProp.getHttpTrackerHttpPort());
        this.logger.info("FastDFS: fastdfs.tracker_servers=" + this.dfsProp.getTrackerServers());
        this.logger.info("FastDFS: fastdfs.connection_pool_max_total=" + this.dfsProp.getConnectionPoolMaxTotal());
        this.logger.info("FastDFS: fastdfs.connection_pool_max_idle=" + this.dfsProp.getConnectionPoolMaxIdle());
        this.logger.info("FastDFS: fastdfs.connection_pool_min_idle=" + this.dfsProp.getConnectionPoolMinIdle());
        this.logger.info("FastDFS: fastdfs.nginx_servers=" + this.dfsProp.getNginxServers());

        prop.put("fastdfs.connect_timeout_in_seconds", this.dfsProp.getConnectTimeout());
        prop.put("fastdfs.network_timeout_in_seconds", this.dfsProp.getNetworkTimeout());
        prop.put("fastdfs.charset", this.dfsProp.getCharset());
        prop.put("fastdfs.http_anti_steal_token", this.dfsProp.getHttpAntiStealToken());
        prop.put("fastdfs.http_secret_key", this.dfsProp.getHttpSecretKey());
        prop.put("fastdfs.http_tracker_http_port", this.dfsProp.getHttpTrackerHttpPort());
        prop.put("fastdfs.tracker_servers", this.dfsProp.getTrackerServers());
        ClientGlobal.initByProperties(prop);
    }

    /**
     * 显示初始化信息
     */
    private void info() {
        this.logger.info("FastDFS parameter: ConnectionPoolMaxTotal ==> " + this.pool.getMaxTotal());
        this.logger.info("FastDFS parameter: ConnectionPoolMaxIdle ==> " + this.pool.getMaxIdle());
        this.logger.info("FastDFS parameter: ConnectionPoolMinIdle ==> " + this.pool.getMinIdle());
        this.logger.info("FastDFS parameter: NginxServer ==> " + Arrays.toString(this.nginxServers));
        this.logger.info(ClientGlobal.configInfo());
    }

    /**
     * 创建连接池
     */
    private void create() {
        GenericObjectPoolConfig<StorageClient> config = new GenericObjectPoolConfig<>();
        this.logger.info("FastDFS Client: Creating connection pool...");
        config.setMaxTotal(Integer.parseInt(this.dfsProp.getConnectionPoolMaxTotal()));
        config.setMaxIdle(Integer.parseInt(this.dfsProp.getConnectionPoolMaxIdle()));
        config.setMinIdle(Integer.parseInt(this.dfsProp.getConnectionPoolMinIdle()));
        StorageClientFactory factory = new StorageClientFactory();
        this.pool = new GenericObjectPool<>(factory, config);
        this.nginxServers = this.dfsProp.getNginxServers().split(",");
    }

    /**
     * Nginx服务器负载均衡算法
     * @param servers 服务器地址
     * @param address 客户端IP地址
     * @return 可用的服务器地址
     */
    private String getNginxServer(String[] servers, String address) {
        int size = servers.length;
        int i = address.hashCode();
        int index = abs(i % size);
        return servers[index];
    }

    /**
     * 带有防盗链的下载
     * @param fileGroup       文件组名
     * @param remoteFileName  远程文件名称
     * @param clientIpAddress 客户端IP地址
     * @return 完整的URL地址
     */
    public String autoDownloadWithToken(String fileGroup, String remoteFileName, String clientIpAddress) throws Exception {
        int ts = (int) (System.currentTimeMillis() / 1000);
        String token = ProtoCommon.getToken(remoteFileName, ts, ClientGlobal.getG_secret_key());
        String nginx = this.getNginxServer(this.nginxServers, clientIpAddress);
        return "http://" + nginx + "/" + fileGroup + "/" + remoteFileName + "?token=" + token + "&ts=" + ts;
    }

    /**
     * 上传文件，适合上传图片
     * @param buffer 字节数组
     * @param ext    扩展名
     * @return 文件组名和ID
     */
    public String[] autoUpload(byte[] buffer, String ext) throws Exception {
        return this.upload(buffer, ext, null);
    }

    /**
     * 不带防盗链的下载，如果开启防盗链会导致该方法抛出异常
     * @param fileGroup       文件组名
     * @param remoteFileName  远程文件ID
     * @param clientIpAddress 客户端IP地址，根据客户端IP来分配Nginx服务器
     * @return 完整的URL地址
     */
    public String autoDownloadWithoutToken(String fileGroup, String remoteFileName, String clientIpAddress) throws Exception {
        if (ClientGlobal.getG_anti_steal_token()) {
            this.logger.error("FastDFS Client: You've turned on Token authentication.");
            throw new Exception("You've turned on Token authentication.");
        }
        String nginx = this.getNginxServer(this.nginxServers, clientIpAddress);
        return "http://" + nginx + "/" + fileGroup + "/" + remoteFileName;
    }

    /**
     * 通过本地文件上传
     * @param localFileName 本地文件名称
     * @param fileExtName   文件扩展名
     * @param metadata      文件的元数据
     * @return 文件组名和ID
     */
    public String[] upload(String localFileName, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        final String[] strings = client.upload_file(localFileName, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过本地文件上传
     * @param localFileName  本地文件名称
     * @param fileExtName    文件扩展名
     * @param metadata       文件的元数据
     * @param waitTimeMillis 获取连接等待时间
     * @return 文件组名和ID
     */
    public String[] upload(String localFileName, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        final String[] strings = client.upload_file(localFileName, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param buff        文件的字节数组
     * @param offset      偏移量
     * @param len         长度
     * @param fileExtName 文件扩展名
     * @param metadata    文件的元数据
     * @return 文件组名和ID
     */
    public String[] upload(byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param buff           文件的字节数组
     * @param offset         偏移量
     * @param len            长度
     * @param fileExtName    文件扩展名
     * @param metadata       文件的元数据
     * @param waitTimeMillis 获取连接的等待时间
     * @return 文件组名和ID
     */
    public String[] upload(byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传
     * @param groupName   文件的组名
     * @param buff        文件的字节数组
     * @param offset      偏移量
     * @param len         长度
     * @param fileExtName 文件扩展名
     * @param metadata    文件的元数据
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param groupName      文件的组名
     * @param buff           文件的字节数组
     * @param offset         偏移量
     * @param len            长度
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 获取连接的等待时间
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传
     * @param fileBuff    文件的字节数组
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] upload(byte[] fileBuff, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(fileBuff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param fileBuff       文件的字节数组
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 获取连接的等待时间
     * @return 文件组名和ID
     */
    public String[] upload(byte[] fileBuff, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(fileBuff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param groupName   文件组名
     * @param fileBuff    文件的字节数组
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件的组名和ID
     */
    public String[] upload(String groupName, byte[] fileBuff, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, fileBuff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param groupName      文件组名
     * @param fileBuff       文件的字节数组
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待连接的时长
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, byte[] fileBuff, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, fileBuff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过回调接口上传文件
     * @param groupName   文件组名
     * @param fileSize    文件大小
     * @param callback    回调函数
     * @param fileExtName 文件扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, long fileSize, UploadCallback callback, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, fileSize, callback, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过回调接口上传文件
     * @param groupName      文件组名
     * @param fileSize       文件大小
     * @param callback       回调接口
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待连接的时长
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, long fileSize, UploadCallback callback, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, fileSize, callback, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过本地上传文件
     * @param groupName      文件组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param localFileName  本地文件名称
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, String localFileName, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, localFileName, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过本地上传文件
     * @param groupName      文件组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param localFileName  本地文件名称
     * @param fileExtName    文件扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待连接的时长
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, String localFileName, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, localFileName, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传
     * @param groupName      文件组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param buff           文件的字节数组
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, byte[] buff, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, buff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传
     * @param groupName      组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param buff           文件的字节数组
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待连接的时长
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, byte[] buff, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, buff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param groupName      组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param buff           文件的字节数组
     * @param offset         偏移量
     * @param len            长度
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传文件
     * @param groupName      组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param buff           文件的字节数组
     * @param offset         偏移量
     * @param len            长度
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待连接的时长
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过回调函数上传文件
     * @param groupName      组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param fileSize       文件的大小
     * @param callback       回调接口
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, long fileSize, UploadCallback callback, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, fileSize, callback, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过回调函数上传文件
     * @param groupName      组名
     * @param masterFileName 生成副本的文件名
     * @param prefixName     生成副本的文件名前缀
     * @param fileSize       文件的大小
     * @param callback       回调接口
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待连接的时长
     * @return 文件组名和ID
     */
    public String[] upload(String groupName, String masterFileName, String prefixName, long fileSize, UploadCallback callback, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_file(groupName, masterFileName, prefixName, fileSize, callback, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 将本地附加文件上传
     * @param localFileName 本地文件名称
     * @param fileExtName   文件扩展名
     * @param metadata      元数据
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String localFileName, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_appender_file(localFileName, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 将附加文件上传
     * @param localFileName  文件名称
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 获取连接等待时长
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String localFileName, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_appender_file(localFileName, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param buff        字节数组
     * @param offset      偏移量
     * @param len         长度
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_appender_file(buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param buff           字节数组
     * @param offset         偏移量
     * @param len            长度
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待获取连接的时长
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_appender_file(buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param groupName   组名
     * @param buff        字节数组
     * @param offset      偏移量
     * @param len         长度
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String groupName, byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_appender_file(groupName, buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param groupName      组名
     * @param buff           字节数组
     * @param offset         偏移量
     * @param len            长度
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 获取连接的等待时长
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String groupName, byte[] buff, int offset, int len, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_appender_file(groupName, buff, offset, len, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param buff        字节数组
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(byte[] buff, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_appender_file(buff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param buff           字节数组
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待获取连接的时长
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(byte[] buff, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_appender_file(buff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传
     * @param groupName   组名
     * @param buff        字节数组
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String groupName, byte[] buff, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_appender_file(groupName, buff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过字节数组上传附加文件
     * @param groupName      组名
     * @param buff           字节数组
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 获取连接的等待时长
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String groupName, byte[] buff, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_appender_file(groupName, buff, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过回调上传附加文件
     * @param groupName   组名
     * @param fileSize    文件大小
     * @param callback    回调函数
     * @param fileExtName 扩展名
     * @param metadata    元数据
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String groupName, long fileSize, UploadCallback callback, String fileExtName, NameValuePair[] metadata) throws Exception {
        StorageClient client = this.pool.borrowObject();
        String[] strings = client.upload_appender_file(groupName, fileSize, callback, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 通过回调上传附加文件
     * @param groupName      组名
     * @param fileSize       文件大小
     * @param callback       回调函数
     * @param fileExtName    扩展名
     * @param metadata       元数据
     * @param waitTimeMillis 等待时间
     * @return 文件组名和ID
     */
    public String[] uploadAppenderFile(String groupName, long fileSize, UploadCallback callback, String fileExtName, NameValuePair[] metadata, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        String[] strings = client.upload_appender_file(groupName, fileSize, callback, fileExtName, metadata);
        this.pool.returnObject(client);
        return strings;
    }

    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param localFileName    本地文件名称
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, String localFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.append_file(groupName, appenderFileName, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param localFileName    本地文件名称
     * @param waitTimeMillis   等待时间时长
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, String localFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.append_file(groupName, appenderFileName, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param buff             文件字节数组
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, byte[] buff) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.append_file(groupName, appenderFileName, buff);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param buff             文件字节数组
     * @param waitTimeMillis   等待时长
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, byte[] buff, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.append_file(groupName, appenderFileName, buff);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param buff             字节数组
     * @param offset           偏移量
     * @param len              长度
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, byte[] buff, int offset, int len) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.append_file(groupName, appenderFileName, buff, offset, len);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param buff             字节数组
     * @param offset           偏移量
     * @param len              长度
     * @param waitTimeMillis   等待时长
     * @return 文件组名和ID
     */
    public int appendFile(String groupName, String appenderFileName, byte[] buff, int offset, int len, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.append_file(groupName, appenderFileName, buff, offset, len);
        this.pool.returnObject(client);
        return i;
    }


    /**
     * 追加文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileSize         文件大小
     * @param callback         回调函数
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, long fileSize, UploadCallback callback) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.append_file(groupName, appenderFileName, fileSize, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 上传文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileSize         文件大小
     * @param callback         回调函数
     * @param waitTimeMillis   等待时间
     * @return 返回0表示成功
     */
    public int appendFile(String groupName, String appenderFileName, long fileSize, UploadCallback callback, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.append_file(groupName, appenderFileName, fileSize, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param localFileName    本地文件名称
     * @return 返回0表示成功
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, String localFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.modify_file(groupName, appenderFileName, fileOffset, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param localFileName    本地文件名称
     * @param waitTimeMillis   等待时长
     * @return 返回0表示成功
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, String localFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.modify_file(groupName, appenderFileName, fileOffset, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param buff             字节数组
     * @return 返回0表示成功
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, byte[] buff) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.modify_file(groupName, appenderFileName, fileOffset, buff);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param buff             字节数组
     * @param waitTimeMillis   等待时间
     * @return 返回0表示成功
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, byte[] buff, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.modify_file(groupName, appenderFileName, fileOffset, buff);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param buff             字节数组
     * @param bufferOffset     偏移量
     * @param bufferLen        长度
     * @return 返回0表示成功
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, byte[] buff, int bufferOffset, int bufferLen) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.modify_file(groupName, appenderFileName, fileOffset, buff, bufferOffset, bufferLen);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       文件偏移量
     * @param buff             字节数组
     * @param bufferOffset     字节偏移量
     * @param bufferLen        字节长度
     * @param waitTimeMillis   等待时间
     * @return 返回0表示成功
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, byte[] buff, int bufferOffset, int bufferLen, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.modify_file(groupName, appenderFileName, fileOffset, buff, bufferOffset, bufferLen);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param modifySize       修改大小
     * @param callback         回调函数
     * @return 文件组名和ID
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, long modifySize, UploadCallback callback) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.modify_file(groupName, appenderFileName, fileOffset, modifySize, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 修改文件
     * @param groupName        组名
     * @param appenderFileName 追加文件名称
     * @param fileOffset       偏移量
     * @param modifySize       修改大小
     * @param callback         回调函数
     * @param waitTimeMillis   等待时间
     * @return 文件组名和ID
     */
    public int modify(String groupName, String appenderFileName, long fileOffset, long modifySize, UploadCallback callback, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.modify_file(groupName, appenderFileName, fileOffset, modifySize, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 删除文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @return 返回0表示成功
     */
    public int delete(String groupName, String remoteFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.delete_file(groupName, remoteFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 删除文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public int delete(String groupName, String remoteFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.delete_file(groupName, remoteFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 文件分片
     * @param groupName    组名
     * @param appenderName 输出源文件名称
     * @return 返回0表示成功
     */
    public int truncate(String groupName, String appenderName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.truncate_file(groupName, appenderName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 文件分片
     * @param groupName      组名
     * @param appenderName   输出源文件名称
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public int truncate(String groupName, String appenderName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        client.truncate_file(groupName, appenderName);
        this.pool.returnObject(client);
        return 0;
    }

    /**
     * 文件分片
     * @param truncatedFileSize 分片大小
     * @param groupName         组名
     * @param appenderName      输出源文件名称
     * @return 返回0表示成功
     */
    public int truncate(long truncatedFileSize, String groupName, String appenderName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.truncate_file(groupName, appenderName, truncatedFileSize);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 文件分片
     * @param truncatedFileSize 分片大小
     * @param groupName         组名
     * @param appenderName      输出源文件
     * @param waitTimeMillis    等待时间
     * @return 返回0表示成功
     */
    public int truncate(long truncatedFileSize, String groupName, String appenderName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.truncate_file(groupName, appenderName, truncatedFileSize);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @return 返回0表示成功
     */
    public byte[] download(String groupName, String remoteFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        byte[] bytes = client.download_file(groupName, remoteFileName);
        this.pool.returnObject(client);
        return bytes;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public byte[] download(String groupName, String remoteFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        byte[] bytes = client.download_file(groupName, remoteFileName);
        this.pool.returnObject(client);
        return bytes;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param offset         偏移量
     * @param bytes          字节数组
     * @return 返回0表示成功
     */
    public byte[] download(String groupName, String remoteFileName, long offset, long bytes) throws Exception {
        StorageClient client = this.pool.borrowObject();
        byte[] bytes1 = client.download_file(groupName, remoteFileName, offset, bytes);
        this.pool.returnObject(client);
        return bytes1;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param offset         偏移量
     * @param bytes          字节数组
     * @param waitTimeMillis 等待时长
     * @return 返回0表示成功
     */
    public byte[] download(String groupName, String remoteFileName, long offset, long bytes, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        byte[] bytes1 = client.download_file(groupName, remoteFileName, offset, bytes);
        this.pool.returnObject(client);
        return bytes1;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param localFileName  本地文件名称
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, String localFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.download_file(groupName, remoteFileName, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param localFileName  本地文件名称
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, String localFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.download_file(groupName, remoteFileName, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param offset         偏移量
     * @param bytes          字节数组
     * @param localFileName  本地文件名称
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, long offset, long bytes, String localFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.download_file(groupName, remoteFileName, offset, bytes, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param offset         偏移量
     * @param bytes          字节数组
     * @param localFileName  本地文件名称
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, long offset, long bytes, String localFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.download_file(groupName, remoteFileName, offset, bytes, localFileName);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param callback       回调函数
     * @return 文件分组和ID
     */
    public int download(String groupName, String remoteFileName, DownloadCallback callback) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.download_file(groupName, remoteFileName, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param callback       回调函数
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, DownloadCallback callback, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.download_file(groupName, remoteFileName, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param offset         偏移量
     * @param bytes          字节数组
     * @param callback       回调函数
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, long offset, long bytes, DownloadCallback callback) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.download_file(groupName, remoteFileName, offset, bytes, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 下载文件
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param offset         偏移量
     * @param bytes          字节数组
     * @param callback       回调函数
     * @param waitTimeMillis 等待时间
     * @return 返回0表示成功
     */
    public int download(String groupName, String remoteFileName, long offset, long bytes, DownloadCallback callback, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.download_file(groupName, remoteFileName, offset, bytes, callback);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 获取元数据
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @return 键值对数组
     */
    public NameValuePair[] getMetadata(String groupName, String remoteFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        NameValuePair[] metadata = client.get_metadata(groupName, remoteFileName);
        this.pool.returnObject(client);
        return metadata;
    }

    /**
     * 获取元数据
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param waitTimeMillis 等待时间
     * @return 键值对数组
     */
    public NameValuePair[] getMetadata(String groupName, String remoteFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        NameValuePair[] metadata = client.get_metadata(groupName, remoteFileName);
        this.pool.returnObject(client);
        return metadata;
    }

    /**
     * 设置元数据
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param metadata       元数据
     * @param flag           更新设置：ProtoCommon.STORAGE_SET_METADATA_FLAG_OVERWRITE 表示重写所有; ProtoCommon.STORAGE_SET_METADATA_FLAG_MERGE 表示没有就插入，有就更新
     * @return 返回0表示成功
     */
    public int setMetadata(String groupName, String remoteFileName, NameValuePair[] metadata, byte flag) throws Exception {
        StorageClient client = this.pool.borrowObject();
        int i = client.set_metadata(groupName, remoteFileName, metadata, flag);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 设置元数据
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param metadata       元数据
     * @param flag           更新设置：ProtoCommon.STORAGE_SET_METADATA_FLAG_OVERWRITE 表示重写所有; ProtoCommon.STORAGE_SET_METADATA_FLAG_MERGE 表示没有就插入，有就更新
     * @param waitTimeMillis 等待时长
     * @return 返回0表示成功
     */
    public int setMetadata(String groupName, String remoteFileName, NameValuePair[] metadata, byte flag, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        int i = client.set_metadata(groupName, remoteFileName, metadata, flag);
        this.pool.returnObject(client);
        return i;
    }

    /**
     * 获取文件信息
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @return 返回文件信息
     */
    public FileInfo getFileInfo(String groupName, String remoteFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        FileInfo file_info = client.get_file_info(groupName, remoteFileName);
        this.pool.returnObject(client);
        return file_info;
    }

    /**
     * 获取文件名称
     * @param groupName      组名
     * @param remoteFileName 远程文件名称
     * @param waitTimeMillis 等待时长
     * @return 返回文件信息
     */
    public FileInfo getFileInfo(String groupName, String remoteFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        FileInfo file_info = client.get_file_info(groupName, remoteFileName);
        this.pool.returnObject(client);
        return file_info;
    }

    /**
     * 查询文件信息
     * @param groupName      组名
     * @param remoteFileName 远程文件信息
     * @return 返回文件信息
     */
    public FileInfo queryFileInfo(String groupName, String remoteFileName) throws Exception {
        StorageClient client = this.pool.borrowObject();
        FileInfo fileInfo = client.query_file_info(groupName, remoteFileName);
        this.pool.returnObject(client);
        return fileInfo;
    }

    /**
     * 查询文件信息
     * @param groupName      组名
     * @param remoteFileName 远程文件信息
     * @param waitTimeMillis 等待时间
     * @return 返回文件信息
     */
    public FileInfo queryFileInfo(String groupName, String remoteFileName, long waitTimeMillis) throws Exception {
        StorageClient client = this.pool.borrowObject(waitTimeMillis);
        FileInfo fileInfo = client.query_file_info(groupName, remoteFileName);
        this.pool.returnObject(client);
        return fileInfo;
    }
}
