**OAuth2 就是「第三方应用，不用拿你的账号密码，就能有限访问你的数据」的一套协议**。 四个角色：**资源服务器、资源拥有者、客户端、授权服务器**，拿一个现实例子类比：

> 你用抖音账号登录某小程序，小程序要读取你的抖音头像、昵称。

## 资源拥有者 Resource owner

**就是人，资源的主人，普通用户** 例子：**你本人**，抖音账号是你的，头像、昵称这些数据是你的资源。 你有权利决定：要不要让别的程序看你的数据。

## 资源服务器 Resource server

**存放受保护数据的服务器，对外提供 API，也就是 API 服务端** 例子：**抖音的资源服务器**，存着你的头像、昵称、视频这些受保护资源。 别人不能随便调用 API 拿你的数据，必须携带合法的访问令牌才给返回数据。

## 授权服务器 Authorization server

**专门做登录、审批、发令牌的服务器** 例子：**抖音授权服务器** 流程：小程序要拿你的信息 → 跳转到抖音登录页（授权服务器），问你：是否允许小程序读取你的头像昵称？

- 如果你点同意，**授权服务器生成一张「访问令牌 access_token」交给小程序（客户端）**；
- 拒绝就什么都不给。

## 完整流程

1. **客户端 (小程序)**：我想要用户的抖音头像；
2. 跳转到**授权服务器 (抖音授权页)**，找**资源拥有者 (你)**问：允许小程序拿你的头像吗？
3. **你（资源拥有者）**点同意；
4. **授权服务器**下发 `access_token`（访问令牌，相当于临时通行证）给小程序；
5. **客户端 (小程序)**拿着这张通行证，请求**资源服务器 (抖音资源 API)**；
6. **资源服务器校验令牌合法**，返回你的头像数据。

## 搭建步骤

### 1.查看Readme.md,如何搭建oauth2模块

D:\01BigProGram2026-7\his-framework\zero-one-cloud-starter\zero-one-cloud-starter-oauth2\readme.md

### 2.看到配置文件的TIP时，需要生成`jwt.jks`

```java
***TIP：***由于使用了`JWT`管理凭证，所以需要自己生成`jwt.jks`，生成过程这里就不在赘述了，`jks`文件名也可以通过配置来指定。

我们可以查看项目自述文件，厘米按告诉你如何生成jks
```

### 3.理解业务流程
### 准备阶段（项目启动，一次性做好）

1. 授权服务器读取本地的 **jks 保险柜文件**
2. 从 jks 取出**私钥**（留在授权服务器，绝不外传）
3. 从 jks 的证书中可以提取出**公钥**。公钥可以对外发布，但 jks 文件本身不给别人。

------

### 运行时业务流程

1. 用户登录授权成功 → **授权服务器用自己早已存好的私钥，签名生成 JWT 令牌**，把 JWT 返回给客户端。

1. **客户端拿着 JWT 令牌，直接去找资源服务器要资源**，不再找授权服务器。
2. 资源服务器本地保存着【公钥】（这个公钥源头来自 jks，但资源服务器没有 jks 文件）
3. 资源服务器拿本地公钥，校验 JWT 里面的签名：
   - 校验通过：返回受保护资源
   - 校验失败：返回 401 无权限

认识4个自定义扩展
**文件一：**`LoadUserDetailServiceImpl.java`

**一句话概括：负责“认人”。**

当用户在登录页输入用户名和密码，点下“登录”按钮时，OAuth2 框架会调用这个类。它做的事情很简单：

1. 拿着用户输入的用户名，去**数据库**里查
2. 查到了 → 把用户信息（用户名、加密后的密码、角色列表）打包成一个对象，交给框架
3. 框架拿到这个对象后，自动比对密码是否正确

**打个比方**：你去酒店办入住，前台（这个文件）拿着你的名字去查系统，确认你确实预订了房间，然后告诉系统“这个人可以入住”。

------

### **文件二：**`TokenEnhancerDataServiceImpl.java`

**一句话概括：负责往 Token 里“塞额外信息”。**

OAuth2 框架生成的 JWT Token 默认只包含最基本的信息（比如用户名、过期时间）。但这个文件会**在 Token 生成之前，往里面多加一些业务字段**，比如用户的 ID、手机号等。

**打个比方**：酒店给你发的房卡（Token），默认只写了“301房间，有效到明天”。但这个文件会额外在房卡上刻上你的“会员卡号”，这样你去酒店餐厅吃饭时，服务员刷一下房卡就知道你是金卡会员，不用再去前台查。

------

### **文件三：**`ResourcesServiceImpl.java`

**一句话概括：负责“定规矩”——哪个接口需要哪个角色才能访问。**

它在项目启动的时候，从数据库里把所有“接口地址（URL）”和“对应角色”的关系查出来，然后**缓存到 Redis** 里。

之后每次有请求过来，网关或过滤器就会去 Redis 里查：“这个用户是普通角色，他要访问的 `/admin/delete` 接口需要管理员角色，所以拒绝访问。”

**打个比方**：酒店开业前，经理（这个文件）把所有房间的权限表贴在墙上——“3楼总统套房只有 VIP 卡能刷开”。保安（网关）看一眼墙就知道让不让进，不用每次都打电话问经理。

------

### **文件四：**`RedisOauth2AuthorizationServiceImpl.java`

**一句话概括：负责把 Token 和授权码“存起来”，而不是存在内存里。**

OAuth2 框架默认把生成的 Token、授权码（Code）都存在**内存**里。但内存有个致命问题：服务一重启就全丢了，而且多台服务器之间数据不共享。

这个文件就是**把存储位置从内存换成了 Redis**，这样：

- 服务重启不会丢数据
- 多台服务器共享同一份 Token 数据（集群部署必备）

**打个比方**：酒店默认把入住记录写在便签纸上贴在前台（内存），换班就丢了。这个文件相当于把入住记录全部录入到**中央电脑系统（Redis）**里，不管哪个前台、哪个分店，都能查到。

------

### **四个文件的协作关系**

用一条登录流程串起来：

> 1. 用户登录 → **文件一**去数据库查用户信息
> 2. 验证通过，准备发 Token → **文件二**往 Token 里塞入用户 ID
> 3. Token 生成后 → **文件四**把 Token 存到 Redis
> 4. 用户拿着 Token 访问接口 → **文件三**提前缓存好的权限规则判断有没有权限

理解以上内容，可以根据业务对注释是TODO的内容进行修改就行，接下来就是网关了

`LoadResourcesDataImpl`

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
        //用请求的 URL 去 Redis 里查
        Object obj = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, url);
		//查不到映射关系 → 说明这个 URL 不需要权限校验 → 网关直接放行
        if (obj == null) {
            return null;
        }
        //查出来的是角色列表，比如 ["ADMIN", "USER"]就返回
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

然后我们再看login的注释，使用的是授权码模式如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口

```java
package com.zeroone.star.project.login;

import com.zeroone.star.project.dto.login.LoginDTO;
import com.zeroone.star.project.dto.login.Oauth2TokenDTO;
import com.zeroone.star.project.dto.login.RefreshTokenDTO;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.login.LoginVO;
import com.zeroone.star.project.vo.login.MenuTreeVO;

import java.util.List;

/**
 * <p>
 * 描述：用户登录接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public interface LoginApis {
    /**
     * 授权登录接口
     * 提示：如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口
     * @param loginDTO 登录数据
     * @return 授权登录结果
     */
    default JsonVO<Oauth2TokenDTO> authLogin(LoginDTO loginDTO) {
        return null;
    }

    /**
     * 刷新Token认证
     * 提示：如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口
     * @param refreshTokenDTO 刷新凭证数据对象
     * @return 刷新Token结果
     */
    default JsonVO<Oauth2TokenDTO> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        return null;
    }

    /**
     * 退出登录
     * 提示：如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口
     * @return 退出结果
     */
    default JsonVO<String> logout(){
        return null;
    }

    /**
     * 获取当前用户信息，登录成功后才能调用，需要通过凭证获取信息的
     * @return 返回当前用户信息
     */
    JsonVO<LoginVO> getCurrUser();

    /**
     * 获取菜单数据，登录成功后才能调用，需要通过凭证获取信息的
     * @return 菜单数据
     */
    JsonVO<List<MenuTreeVO>> getMenus();
}

```

我们再看login模块，其中有远程调用aouth模块暴露的端点
**Spring Authorization Server（基于 Spring Security）框架内置的**，自动暴露的 OAuth2 标准端点。

```java
package com.zeroone.star.login.service;

import com.zeroone.star.login.fallback.OauthServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * <p>
 * 描述：授权声明式服务接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@FeignClient(value = "${sn.auth}", fallbackFactory = OauthServiceFallbackFactory.class)
public interface OauthService {
    /**
     * 记录错误信息的key
     */
    String ERROR_KEY = "error";

    /**
     * 获取认证令牌
     * @param parameters 参数列表
     * @return 结果数据
     */
    @PostMapping(value = "/oauth2/token", consumes = "application/x-www-form-urlencoded")
    Map<String, Object> postAccessToken(@RequestBody Map<String, String> parameters);

    /**
     * 撤销认证令牌
     * @param parameters 参数列表
     * @return 结果数据
     */
    @PostMapping(value = "/oauth2/revoke", consumes = "application/x-www-form-urlencoded")
    Map<String, Object> revokeToken(@RequestBody Map<String, String> parameters);
}
```

了解完部分文件是干什么的就可以看是测试登录接口了，首先启动gateway服务要关掉聚合文档，启动login，oauth2服务

```yml
server:
  port: ${sp.gateway}
spring:
  application:
    name: ${sn.gateway}
  security:
    oauth2:
      resourceserver:
        jwt:
          # 公钥文件配置
          public-key-location: classpath:public.pem
  cloud:
    gateway:
      server:
        webflux:
          #          # 动态创建路由的功能
          #          # 路由规则是以微服务名称为前缀（如：/微服务名/真实路径），转发的的过程中会去掉前缀
          #          # 注意自动路由优先级比较高，所以如果发现手动路由配置不生效，
          #          # 通过指定一个较小的order值来保证手动路由生效。
          #          # 一般情况下我们使用手动创建路由，这里只是给大家说明一下有这个功能，因为自己的服务模块可能会有特殊需求。
          #          discovery:
          #            locator:
          #              enabled: true
          #              lower-case-service-id: true
          # 下面是手动创建路由路径配置
          # 提示：
          # predicates中如果要匹配多个地址前缀，每个前缀用,分割，如：- Path=/auth/**,/oauth/**
          # filters中StripPrefix=1表示去掉Path中的第一个前缀
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
          - "/doc.html"
          - "/v3/api-docs/**"
          - "/*/v3/api-docs"
          - "/webjars/**"
        # 配置内部路径(需要用到凭证但不需要权限验证的请求)
        inner-paths:
          - "/login/current-user"
          - "/login/get-menus"
          - "/login/logout"
          - "/chat/**"

knife4j:
  gateway:
    # 开启gateway聚合组件，生产环境不开,原本是开着的true
    enabled: false
    # 指定服务发现的模式聚合微服务文档，并且是默认`default`分组
    strategy: discover
    discover:
      enabled: true
      # 指定版本号(Swagger2|OpenAPI3)
      version: openapi3
      # 需要排除的微服务
      excluded-services:
        - ${sn.gateway}
        - ${sn.auth}

```

理解组件二文档内容如何在apifox中创建aouth2组件用来获取token，点击获取token就会跳转到登录页面，然后进行登录
最后我们打开swagger文档进行测试http://localhost:10200/login/doc.html
将我们拿到的token放入authorize中，就可以进行接口测试了

最后可以通过redis可视化工具查看到redis存了哪些用户信息