# 工程简介

对Gateway鉴权进行二次starter封装，方便在项目架构中集成网关鉴权

其中主要内置的操作包括：

- 资源服务配置和动态鉴权管理器的实现
- 内置三个过滤器：白名单、跨域、`Jwt`数据转换处理
- 并提供三个扩展接口：
  - 资源加载接口，用于适配接入系统的权限资源加载
  - 响应数据包装，用于适配接入系统的统一响应数据格式
  - 凭证扩展校验，用于提供凭证扩展处理业务，如：处理凭证注销操作


# 延伸阅读

## 外部集成使用步骤

### 1 导入依赖

```xml
<!-- zo gateway cloud starter -->
<dependency>
    <groupId>com.zeroone.star</groupId>
    <artifactId>zero-one-cloud-starter-gateway</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

***TIP：***当然要导入这个依赖需要使用`mvn install` 命令将源码编译并安装到你的本地仓库中

### 2 修改项目配置

修改项目的`application.yaml`配置文件，添加如下配置

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # 公钥文件配置
          public-key-location: classpath:public.pem
zo:
  cloud:
    starter:
      gateway:
        # 配置是否开启鉴权
        open-authorization: true
        # 配置白名单路径
        white-paths:
          - "/actuator/**"
          - "/captcha/**"
          - "/auth/oauth2/token"
          - "/login/auth-login"
          - "/login/refresh-token"
        # 配置内部路径(需要用到凭证但不需要权限验证的请求)
        inner-paths:
          - "/login/current-user"
          - "/login/get-menus"
```

***TIPS：其中的`jwt`公钥需要和你在`Oauth2`中使用的私钥是配对的才行。***

除此之外当然还有你的网关路由配置，需要根据你的自己的情况来配置，下面是一个简单的示例：

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              # 开启从注册中心动态创建路由的功能
              enabled: true
          # 注意：这里的路径配置需要移植到nacos配置中心system.yaml中
          # 提示：predicates 中如果要匹配多个地址前缀，每个前缀用,分割，如：- Path=/auth/**,/oauth/**
          routes:
            - id: oauth2-auth-route
              uri: lb://${sn.auth}
              predicates:
                - Path=/auth/**
              filters:
                - StripPrefix=1
            - id: login-route
              uri: lb://${sn.login}
              predicates:
                - Path=/login/**
```

### 3 创建配置类

创建一个配置来启用starter，下面是一个示例配置类

```java
import com.zeroone.cloud.starter.gateway.annotation.EnableZoGateway;
import org.springframework.context.annotation.Configuration;
/**
 * <p>
 * 描述：Gateway配置类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
@EnableZoGateway
public class GatewayConfig {
}
```

### 4 实现扩展服务接口

实现加载资源数据服务接口，下面是一段示例代码，具体实现根据你数据来源来实现

```java
import cn.hutool.core.convert.Convert;
import com.zeroone.cloud.starter.gateway.service.LoadResourcesData;
import com.zeroone.star.project.constant.RedisConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
/**
 * <p>
 * 描述：加载资源数据接口实现，一般是从Redis缓存中获取资源数据
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class LoadResourcesDataImpl implements LoadResourcesData {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<String> loadRolesByUrlPath(String url) {
        Object obj = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, url);
        if (obj == null) {
            return null;
        }
        return Convert.toList(String.class, obj);
    }
}
```

实现响应数据包装器，下面是一段示例代码，具体实现根据你系统设计来

```java
import com.zeroone.cloud.starter.gateway.service.ResponseDataWrapper;
import com.zeroone.star.project.vo.JsonVO;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 描述：使用JsonVO对下发数据进行包装
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class ResponseDataWrapperImpl implements ResponseDataWrapper {
    @Override
    public Object executeWrap(String code, String message, Object data) {
        return JsonVO.create(data, Integer.parseInt(code), message);
    }
}
```

实现凭证扩展校验接口，下面是一段示例代码，具体实现根据自己系统的设计来处理

```java
import com.zeroone.cloud.starter.gateway.service.TokenExtendsValidate;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 描述：这里实现注销凭证校验处理
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class TokenExtendsValidateImpl implements TokenExtendsValidate {
    @Override
    public boolean isLogout(String token) {
        return false;
    }
}
```

