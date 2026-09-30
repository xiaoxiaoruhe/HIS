# fastdfs-spring-boot-starter

SpringBoot的高性能FastDFS客户端。避免手动引入jar包导致项目混乱，提供常用的API，有助于快速上手开发。

- 自动添加依赖
- 初始化配置项
- 基于Commons Pool2 实现的高性能连接池
- 更多操作FastDFS的API
- 支持多Tracker多Storage多NGINX负载均衡模式
- 基于[fastdfs-client-java](https://gitee.com/fastdfs100/fastdfs-client-java)(1.39-SNAPSHOT)源代码构建

## 使用教程

### 1 添加依赖

jar包已经发布到制品库，可以直接依赖即可

```xml
<dependency>
    <groupId>com.zeroone.star</groupId>
    <artifactId>fastdfs-spring-boot-starter</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### 2 启动stater支持

在主配置类上添加注解 `@EnableFastdfsClient`

```java
@EnableFastdfsClient
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```
### 3 添加配置条目

下面是application.properties示例

```properties
fastdfs.charset=UTF-8
fastdfs.connect-timeout=5
fastdfs.network-timeout=30
fastdfs.http-secret-key=FastDFS1234567890
fastdfs.http-anti-steal-token=true
fastdfs.connection-pool-max-idle=18
fastdfs.connection-pool-min-idle=2
fastdfs.connection-pool-max-total=18
fastdfs.nginx-servers=192.168.80.2:8888,192.168.80.3:8888,192.168.80.4:8888
fastdfs.tracker-servers=192.168.80.2:22122,192.168.80.3:22122,192.168.80.4:22122
```

下面是application.yml示例

```yaml
fastdfs:
  charset: UTF-8
  connect-timeout: 5
  network-timeout: 30
  http-secret-key: FastDFS1234567890
  http-anti-steal-token: true
  connection-pool-max-idle: 20
  connection-pool-max-total: 20
  connection-pool-min-idle: 2
  nginx-servers: 192.168.80.2:8888,192.168.80.3:8888,192.168.80.4:8888
  tracker-servers: 192.168.80.2:22122,192.168.80.3:22122,192.168.80.4:22122
```

### 4 使用示例

```java
// 注入服务
@Autowired
private FastdfsClientService remoteService;

// 上传文件
try {
    String[] remoteInfo = remoteService.autoUpload(image.getBytes(), type);
    log.info("上传的服务器分组: " + remoteInfo[0]);
    log.info("上传的服务器ID: " + remoteInfo[1]);
} catch (Exception e) {
    log.error("Upload file error: " + e.getMessage());
}

// 下载文件
try {
	byte[] buff = remoteService.download(groupName,remoteFileName);
} catch (Exception e) {
    log.error("Download file error: " + e.getMessage());
}
```

常用函数

```java
// 当启用防盗链机制时,需要使用该方法下载文件
public String autoDownloadWithToken(String fileGroup, String remoteFileName, String clientIpAddress)
// 当没有启用防盗链机制时,需要使用该方法下载文件
public String autoDownloadWithoutToken(String fileGroup, String remoteFileName, String clientIpAddress)
// 上传文件，适合上传图片
public String[] autoUpload(byte[] buffer, String ext)
// 下载文件
public byte[] download(String groupName, String remoteFileName)
// 删除文件
public int delete(String groupName, String remoteFileName)
```

