# 工程简介

对`OAuth2`认证服务进行二次starter封装，方便在项目架构集成与使用`Oauth2`

其中主要内置的操作包括：

- 授权服务配置和Security安全配置
- `UserDetailService`默认实现，并提供外部数据库操作扩展接口
- 凭证增强默认实现，并提供了外部数据组装扩展接口
- 开放扩展接口前缀为 `/open`，如果你再项目中要扩展接口需要保证接口访问前缀正确。

`SAS`暴露端点说明

| **端点**      | URI                          |
| ------------- | ---------------------------- |
| 获取token     | /oauth2/token                |
| 核验token     | /oauth2/introspect           |
| 撤回token     | /oauth2/revoke               |
| 获取授权码    | /oauth2/authorize            |
| 获取设备码    | /oauth2/device_authorization |
| 获取`JWT`公钥 | /oauth2/jwks                 |

# 延伸阅读

## 外部集成使用步骤

### 1 导入依赖

```xml
<!-- zo oauth2 cloud starter -->
<dependency>
    <groupId>com.zeroone.star</groupId>
    <artifactId>zero-one-cloud-starter-oauth2</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
<!-- spring session -->
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

导入spring session是为了将session持久化到`redis`，方便存储数据如client_id

***TIP：***当然要导入这个依赖需要使用`mvn install` 命令将源码编译并安装到你的本地仓库中

### 2 修改项目配置

修改项目的`application.yaml`配置文件，添加如下配置

```yaml
zo:
  cloud:
    starter:
      # Oauth2 Starter配置
      oauth2:
      	# 是否启用，默认值true
        enabled: true
        # 密钥库文件名，默认值jwt.jks
        jks: jwt.jks
        # 密钥库密码，默认值123456
        jks-password: 123456
        # 密钥别名，默认值01star
        key-alias: 01star
        # 密钥密码，默认值123456
        key-password: 123456
spring:
  # Spring Session配置
  session:
    # session超时时间
    timeout: 3600s
    redis:
      # session命名空间
      namespace: spring:session:oauth2
  security:
    oauth2:
      authorizationserver:
        # 注册客户端配置，这个就比较重要了，一定要配置
        client:
          # 管理端配置
          mgr-client:
            registration:
              # 客户端id
              client-id: "project-manager"
              # 认证模式，授权码模式使用none
              client-authentication-methods:
                - "none"
              # 授权模式
              authorization-grant-types:
                - "authorization_code"
              # 登录成功前端回调地址
              redirect-uris:
                - "http://localhost:3000/signin-callback"
              # 登出成功前端回调地址
              post-logout-redirect-uris:
                - "http://localhost:3000/"
              # 授权范围
              scopes:
                - "openid"
                - "profile"
            token:
              # 令牌有效期
              access-token-time-to-live: 10h
              # 刷新令牌有效期
              refresh-token-time-to-live: 24h
            # 是否需要用户确认授权
            require-authorization-consent: false
            # 单页应用使用PKCE支持
            require-proof-key: true
```

***TIP：***由于使用了`JWT`管理凭证，所以需要自己生成`jwt.jks`，生成过程这里就不在赘述了，`jks`文件名也可以通过配置来指定。

### 3 创建配置类

因为前端服务器可能会直接访问认证服务（使用授权码模式）来完成认证过程，所以一般需要你提供跨域配置

```java
/**
 * <p>
 * 描述：Oauth2配置类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
public class Oauth2Config {
    /**
     * 跨域配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://xxx1", "http://xxx2"));
        // 允许所有方法
        config.setAllowedMethods(List.of("*"));
        // 允许所有头
        config.setAllowedHeaders(List.of("*"));
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 暴露所有头
        config.setExposedHeaders(List.of("*"));
        // 预检缓存时间
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### 4 实现扩展服务接口

实现加载用户数据服务接口，下面是一段示例代码，具体实现根据你的业务系统数据库设计来

```java
/**
 * <p>
 * 描述：加载用户信息服务实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class LoadUserDetailServiceImpl implements LoadUserDetailService {
    @Override
    public SecurityUser loadUserDetail(String username, String clientId) 
        throws UsernameNotFoundException {
        if ("管理端".equals(clientId)) {
            return loadUserDetailForMgr(username);
        } else if ("用户端".equals(clientId)) {
            return loadUserDetailForUser(username);
        }
        throw new UsernameNotFoundException("登录客户端ID错误");
    }

    private SecurityUser loadUserDetailForMgr(String username) 
        throws UsernameNotFoundException {
        // TODO：通过用户名查询用户,需要根据你的数据库设计来修改代码
        // 1 通过用户名查找用户对象
        User user = new User();
        user.setUsername(username);
        user = userService.getOne(new QueryWrapper<>(user));
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        // 设置密码加密方式
        user.setPassword("{bcrypt}" + user.getPassword());
        // TODO：通过用户编号查询角色,需要根据你的数据库设计来修改代码
        // 2 通过用户ID获取角色列表
        List<Role> roles = roleService.listRoleByUserId(user.getId());
        // 3 构建权限角色对象
        return SecurityUser.create(user, user.getUsername(), user.getPassword(), 
                                   roles.stream().map(Role::getKeyword)
                                   .collect(Collectors.toList()));
    }

    private SecurityUser loadUserDetailForUser(String username) 
        throws UsernameNotFoundException {
        // TODO：用户端查找用户尚未实现
        System.out.println(username);
        throw new UsernameNotFoundException("用户端查找用户尚未实现");
    }
}
```

实现凭证增强数据组装服务接口，下面是一段示例代码，具体实现根据你的业务需求来

```java
/**
 * <p>
 * 描述：Token增强数据处理服务实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class TokenEnhancerDataServiceImpl implements TokenEnhancerDataService {
    @Override
    public Map<String, Object> enhance(SecurityUser securityUser) {
        Map<String, Object> info = new HashMap<>();
        // 如果是管理端数据库用户DO对象
        if (securityUser.getExtendsObject() instanceof User user) {
            // FIXME: 如果要扩展凭证中的负载数据,需要修改这里的代码
            // 把用户ID设置到JWT中
            info.put("id", user.getId());
        }
        return info;
    }
}
```

### 5 自定义登录页面[可选]

如果你使用的授权码模式来完成认证，这一步是必须的。

因为内置的登录页面比较原始，并且不支持传递client_id，所以需要我们自定登录页面。

#### 5.1 登录页面

登录页面需要使用`thymeleaf`模板来完成书写，下面是文件存放要求，不能放错位置：

- 登录页面存储到 `resources/templates/login.html`
- 样式文件存储到 `resources/static/css/` 目录下面
- 图片文件存储到 `resources/static/images/` 目录下面
- 脚本文件存储到 `resources/static/js/` 目录下面

下面是示例代码

```html
<html xmlns:th="http://www.thymeleaf.org" lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>登录 - 01星球</title>
    <link rel="stylesheet" href="css/login.css"/>
</head>
<body>
<div class="login-card">
    <div class="logo">
        <!-- 01星球 Logo 占位符，可替换为你的真实 SVG -->
        <svg viewBox="0 0 75 75" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="37.5" cy="37.5" r="35" stroke="#1a73e8" stroke-width="3" fill="white"/>
            <text x="37.5" y="45" text-anchor="middle" fill="#1a73e8" font-size="24" 
                  font-weight="bold" dy=".3em">01
            </text>
        </svg>
    </div>
    <h1>登录您的账号</h1>
    <div class="subtitle">使用您的 01星球 账号继续</div>
    <!-- 显示错误信息（如果有） -->
    <div th:if="${param.error}" class="error-message">
        用户名或密码错误，请重试。
    </div>
    <div th:if="${param.logout}" class="error-message"
         style="background:#e8f0fe; color:#1a73e8; border-left-color:#1a73e8;">
        您已成功登出。
    </div>
    <form th:action="@{/login}" method="post">
        <div class="input-group">
            <label for="username">账号</label>
            <input type="text" id="username" name="username" 
                   placeholder="请输入账号" autofocus required>
        </div>
        <div class="input-group">
            <label for="password">密码</label>
            <input type="password" id="password" name="password" 
                   placeholder="请输入密码" required>
        </div>
        <input type="hidden" name="client_id" th:value="${client_id}"/>
        <button type="submit">登录</button>
    </form>
</div>
</body>
</html>
```

#### 5.2 登录接口

需要定义一个控制器来指向登录页面，以及处理client_id的传递，下面是示例代码

```java
/**
 * <p>
 * 描述：自定义登录页控制器
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Controller
public class LoginController {
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, 
                        Model model) {
        // 从Session中获取被拦截前的原始请求
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            // 提取 client_id 参数
            String[] clientIds = savedRequest.getParameterValues("client_id");
            if (clientIds != null && clientIds.length > 0) {
                String clientId = clientIds[0];
                model.addAttribute("client_id", clientId);
                // 存入Session供UserDetailsService后面使用
                request.getSession().setAttribute("client_id", clientId);
            }
        }
        return "login";
    }
}
```

### 6 自定义认证服务[可选]

默认情况下，认证服务将认证信息存储到应用内存中的，如果你想要把他存储到数据库中，用于共享或持久化，此时就需要自定义认证服务。

`SAS`默认提供了两个实现：

- `InMemoryOAuth2AuthorizationService`：内存存储模式
- `JdbcOAuth2AuthorizationService`：关系数据库存储模式

在分布式场景中，通常需要共享一些认证数据，可以把数据存储到`Redis`中，这样其他服务模块就能方便快速的获取数据。

#### 6.1 服务实现

下面是一个简单的示例，演示将数据存储`Redis`中

```java
/**
 * <p>
 * 描述：用于将授权信息存储到Redis中
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public class RedisOauth2AuthorizationServiceImpl implements OAuth2AuthorizationService {
    /**
     * RedisTemplate对象，用于操作Redis数据库
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 创建一个Redis序列化器,使用JDK序列化器来序列化值
     */
    private final RedisSerializer<Object> valueSerializer = 
        new JdkSerializationRedisSerializer();
    /**
     * 创建一个Redis序列化器,使用String序列化器来序列化键
     */
    private final RedisSerializer<String> stringSerializer = 
        new StringRedisSerializer();
    /**
     * 缓存的TTL（10小时）
     */
    private static final Duration TTL = Duration.ofHours(10);

    /**
     * 构造函数，传入RedisTemplate对象
     * @param redisTemplate RedisTemplate对象
     */
    public RedisOauth2AuthorizationServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 动态计算Token的剩余有效期
     */
    private Duration calculateTtl(OAuth2Authorization.Token<? extends OAuth2Token> token) {
        return Optional.ofNullable(token.getToken().getExpiresAt())
                .map(expiresAt -> Duration.between(Instant.now(), expiresAt))
                // 如果过期了（负数或零），给一个极小值（1秒），让它在Redis里立即失效
                .map(duration -> duration.isNegative() ? Duration.ofSeconds(1) : duration)
                // 如果没有过期时间，使用全局默认保底时间 (10h)
                .orElse(TTL);
    }

    /**
     * 保存Token索引
     * @param auth 授权信息
     */
    private void saveTokenIndexes(OAuth2Authorization auth) {
        final String authId = auth.getId();
        // 映射所有类型的Token索引
        record TokenInfo(String type, String value, Duration ttl) {
        }
        List<TokenInfo> tokenInfos = new ArrayList<>();

        // 映射授权码 (Code)
        Optional.ofNullable(auth.getToken(OAuth2AuthorizationCode.class))
                .ifPresent(t -> tokenInfos
                           .add(new TokenInfo("code", t.getToken().getTokenValue(), 
                                              calculateTtl(t))));
        // 映射 AccessToken
        Optional.ofNullable(auth.getAccessToken())
                .ifPresent(t -> tokenInfos
                           .add(new TokenInfo("access_token", t.getToken().getTokenValue(), 
                                              calculateTtl(t))));
        // 映射 RefreshToken
        Optional.ofNullable(auth.getRefreshToken())
                .ifPresent(t -> tokenInfos
                           .add(new TokenInfo("refresh_token", t.getToken().getTokenValue(), 
                                              calculateTtl(t))));
        // 映射 ID Token (用于OIDC退出登录)
        Optional.ofNullable(auth.getToken(OidcIdToken.class))
                .ifPresent(t -> tokenInfos
                           .add(new TokenInfo("id_token", t.getToken().getTokenValue(), 
                                              calculateTtl(t))));

        // 批量写入 Redis 索引
        tokenInfos.forEach(info -> redisTemplate.opsForValue()
                           .set(buildKey(info.type(), info.value()), authId, info.ttl()));
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        // 1. 判断是否已被撤销
        if (isInvalidated(authorization)) {
            this.remove(authorization);
            return;
        }

        // 2. 正常保存逻辑
        // 使用JDK序列化（byte[]）保存主记录，确保SAS内部类100%兼容
        String idKey = buildKey("id", authorization.getId());
        byte[] keyBytes = stringSerializer.serialize(idKey);
        byte[] valueBytes = valueSerializer.serialize(authorization);
        Boolean result = redisTemplate
            .execute((RedisCallback<Boolean>) connection -> connection
                     .stringCommands().setEx(keyBytes, TTL.toSeconds(), valueBytes));
        if (!result) {
            return;
        }

        // 3. 建立索引映射
        saveTokenIndexes(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");

        // 构建Redis的key
        String idKey = buildKey("id", id);
        byte[] keyBytes = stringSerializer.serialize(idKey);

        // 强制以原始字节读取，避开外部String/Jackson序列化器
        byte[] valueBytes = redisTemplate
            .execute((RedisCallback<byte[]>) connection -> connection
                     .stringCommands().get(keyBytes));

        // 反序列化成OAuth2Authorization对象
        return (OAuth2Authorization) valueSerializer.deserialize(valueBytes);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        String id = null;
        if (tokenType != null) {
            // 如果有明确类型，按类型拼接key查找
            id = (String) redisTemplate.opsForValue()
                .get(buildKey(tokenType.getValue(), token));
        } else {
            // 依次尝试access_token,refresh_token,code
            String[] types = {"access_token", "refresh_token", "code", "id_token"};
            for (String type : types) {
                id = (String) redisTemplate.opsForValue().get(buildKey(type, token));
                if (id != null) {
                    break;
                }
            }
        }
        return id != null ? findById(id) : null;
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        // 所有需要删除的Key，自己补充
        List<String> keys = List.of(".......");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 构建索引Key
     * @param type  索引类型
     * @param value 索引值
     * @return 索引Key
     */
    private String buildKey(String type, String value) {
        return "oauth2:auth:" + type + ":" + value;
    }

    /**
     * 判断凭证是否被撤销
     * @param authorization 授权对象
     * @return 如果撤销则返回true
     */
    private boolean isInvalidated(OAuth2Authorization authorization) {
        // 如果AccessToken或RefreshToken任何一个被标记为无效，则认为认证被撤销
        return Stream.of(
                        Optional.ofNullable(authorization.getAccessToken())
            .map(OAuth2Authorization.Token::isInvalidated),
                        Optional.ofNullable(authorization.getRefreshToken())
            .map(OAuth2Authorization.Token::isInvalidated)
                )
                .flatMap(Optional::stream)
                .anyMatch(Boolean::booleanValue);
    }
}
```

#### 6.2 服务注入

服务类写好后注入到容器中，在`Oauth2Config`注入即可，下面是注入示意

```java
/**
 * <p>
 * 描述：Oauth2配置类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
public class Oauth2Config {
    // 省略其他注入

    /**
     * 创建授权服务, 将授权信息存储到Redis中
     * @param redisTemplate RedisTemplate 用于操作Redis的模板对象
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(
        RedisTemplate<String, Object> redisTemplate) {
        return new RedisOauth2AuthorizationServiceImpl(redisTemplate);
    }
}
```

### 7 自定义授权模式[可选]

如果你的业务需要自定义授权模式，如短信验证码登录，可以按照下面的步骤来扩展

#### 7.1 实现认证转换器

starter已经将转换逻辑做了封装，只需要继承抽象转换器，实现抽象方法就可以了

下面是一个简单的示例

```java
/**
 * <p>
 * 描述：短信验证码授权转换器
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public class SmsCodeAuthenticationConverter extends 
    AbstractCustomAuthenticationConverter<CustomGrantAuthenticationToken> {
    /**
     * 构造方法
     * @param grantType 授权模式名称
     */
    public SmsCodeAuthenticationConverter(String grantType) {
        super(grantType);
    }

    @Override
    protected CustomGrantAuthenticationToken createAuthentication
        (Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        // 从参数Map中提取参数
        String phone = (String) additionalParameters.get("phone");
        String code = (String) additionalParameters.get("code");
        // 参数校验
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }
        // 创建授权信息
        return new CustomGrantAuthenticationToken
            (grantType, clientPrincipal, additionalParameters);
    }
}
```

#### 7.2 实现认证提供者

starter已经将转换逻辑做了封装，只需要继承抽象转换器，实现抽象方法就可以了

下面是一个简单的示例

```java
/**
 * <p>
 * 描述：短信验证码授权提供者
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public class SmsCodeAuthenticationProvider extends AbstractCustomAuthenticationProvider {
    /**
     * 构造方法
     * @param authorizationService 认证服务
     * @param tokenGenerator       Token生成器
     */
    public SmsCodeAuthenticationProvider(
        OAuth2AuthorizationService authorizationService, 
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        super(authorizationService, tokenGenerator);
    }

    @Override
    protected Authentication authenticateUser(
        CustomGrantAuthenticationToken token, RegisteredClient registeredClient) {
        String phone = (String) token.getAdditionalParameters().get("phone");
        String code = (String) token.getAdditionalParameters().get("code");

        // 模拟业务校验逻辑
        if (!"123456".equals(code)) {
            throw new OAuth2AuthenticationException("短信验证码错误");
        }

        // 构造认证成功的Principal (通常从数据库查出UserDetails)
        User user = new User();
        user.setUsername(phone);
        user.setPassword("");
        user.setId(1);
        SecurityUser sUser = SecurityUser
            .create(user, user.getUsername(), user.getPassword(), List.of("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(sUser, null, sUser.getAuthorities());
    }
}
```

#### 7.3 自定义策略服务

实现策略服务将自定义授权模式逻辑注入到系统中

下面是示例代码

```java
/**
 * <p>
 * 描述：短信验证码授权策略
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class SmsCodeStrategy implements TokenGranterStrategy {
    @Override
    public String getGrantType() {
        return "sms_code";
    }

    @Override
    public AuthenticationConverter getConverter() {
        return new SmsCodeAuthenticationConverter(getGrantType());
    }

    @Override
    public AuthenticationProvider getProvider(
        OAuth2AuthorizationService authorizationService, 
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        return new SmsCodeAuthenticationProvider(authorizationService, tokenGenerator);
    }
}
```

#### 7.4 配置中添加授权模式

配置文件中新增你的授权模式，比如下面我新增一个认证客户端断点

```yaml
spring:
  # .......
  # 配置认证服务
  security:
    oauth2:
      authorizationserver:
        client:
          # .......
          # 自定义短信验证码登录
          sms-login:
            registration:
              client-id: "sms-login"
              # 客户端认证密码，注意格式
              client-secret: "{noop}123456"
              client-authentication-methods:
                - "client_secret_post"
              authorization-grant-types:
                - "sms_code"
                - "refresh_token"
              scopes:
                - "openid"
                - "profile"
            token:
              access-token-time-to-live: 10h
              refresh-token-time-to-live: 24h
```

处理这些之后，你就可以通过 `/auth2/token`端点来获取凭证了

比如上面的配置，可以通过如下方式来认证

```shell
curl --location --request POST 'http://localhost:10400/oauth2/token' \
--data-urlencode 'client_id=sms-login' \
--data-urlencode 'client_secret=123456' \
--data-urlencode 'grant_type=sms_code' \
--data-urlencode 'phone=13200000000' \
--data-urlencode 'code=12345'
```

