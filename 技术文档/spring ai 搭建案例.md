# Spring AI搭建案例

本项目中我们使用Spring AI Alibaba完成AI功能集成，相关帮助文档，请参考**帮助文档**文件夹中的SpringAIAlibaba帮助文档

## 申请并且配置apikey

DashScope（阿里云百炼）：访问 https://bailian.console.aliyun.com/?apiKey=1&tab=api#/api

其他模型提供商请 参考 SpringAIAlibaba帮助文档



在nacos上的mcp-gateway.yaml修改nacos与本机IP地址

```yaml
spring:
  ai:
    alibaba:
      mcp:
        nacos:
          namespace: project-dev
          #nacos
          server-addr: 192.168.239.128:8848
          username: nacos
          password: nacos
          register:
            enabled: true
            #本机ipv4
            host: 192.168.126.1
        gateway:
          enabled: true
          registry: nacos
          nacos:
            service-names:
              - mcp-nacos-sample
```

在his-mcp-gateway模块中

```yaml
server:
  port: ${sp.mg}
spring:
  application:
    name: ${sn.mg}
  config:
    import:
      # 加载mcp网关配置文件,有其他要引入的配置可以加
      - nacos:mcp-gateway.yaml?refreshEnabled=true
```

按照组件3要求配置环境变量
配置application.yaml

```yaml
server:
  port: ${sp.ai}
  servlet:
    # 设置应用上下文路径
    context-path: /ai
spring:
  application:
    name: ${sn.ai}
  ai:
    # 配置DashScope模型
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          # 模型类型，如：qwen-max、qwen-turbo、qwen-plus
          model: glm-5.2
          # 核采样温度, 取值范围[0.0,1.0]，越小越模型越严谨、保守、确定, 越大模型越有创造力、越活泼、越容易天马行空
          temperature: 0.7
          # 限制大模型单次回答你时最多能吐出多少个字（1000个Token在中文里大约相当于600-800个汉字）
          max_tokens: 6000
          # 核采样, 用来过滤掉那些太离谱、太奇怪的词, 取值范围[0.0,1.0], 为1表示不限制，所有的词都参与备选，越小模型越死板严谨
          top_p: 0.9
    # 配置MCP客户端
    mcp:
      client:
        enabled: true
        name: my-mcp-client  # 客户端名称，需根据业务自行修改
        version: 1.0.0
        type: SYNC
    alibaba:
      # 配置MCP
      mcp:
        nacos:
          # 配置MCP Nacos客户端
          client:
            enabled: true  # 是否启用，必须配置否则找不到工具
            # sse、streamable、configs 等配置需根据实际业务需求补充和完善
            streamable:
              connections:
                server1:
                  service-name: mcp-nacos-gateway
                  version: 1.0.0
            configs:
              server1:
                namespace: his-dev
                server-addr: ${spring.cloud.nacos.server-addr}
                username: ${spring.cloud.nacos.username}
                password: ${spring.cloud.nacos.password}
      # 配置A2A模型
      a2a:
        nacos:
          server-addr: ${spring.cloud.nacos.server-addr}
          username: ${spring.cloud.nacos.username}
          password: ${spring.cloud.nacos.password}
          namespace: his-dev
          discovery:
            # 启用服务发现（查询其他 Agent）
            enabled: true
```

Swagger配置

```java
package com.zeroone.star.ai.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 描述：初始化自定义组件
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 01star
 * @version 1.0.0
 */
@Configuration
@ComponentScan({
        "com.zeroone.star.project.config.swagger"
})
public class ComponentInit {

}

```

请求认证头配置

```java
package com.zeroone.star.ai.config;

import com.zeroone.star.project.config.filter.mcp.RequestHeaderContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * <p>
 * 描述：MCP 客户端请求头配置，在 MCP Gateway 调用下游 MCP Server 时，
 * 自动将当前请求的 Authorization 头透传到工具调用请求中
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 01star
 * @version 1.0.0
 */
@Configuration
@ComponentScan({
        "com.zeroone.star.project.config.filter.mcp"
})
public class McpHeaderConfig {
    /**
     * 自定义 WebClient，
     * 在每次 HTTP 请求前从 RequestHeaderContext 读取 Authorization 并注入请求头
     */
    @Bean
    public WebClientCustomizer mcpWebClientCustomizer() {
        return builder -> builder.filter(this::addAuthorizationHeader);
    }

    @NotNull
    private Mono<ClientResponse> addAuthorizationHeader(
            ClientRequest request, ExchangeFunction next) {
        String authorization = RequestHeaderContext.getAuthorization();
        if (StringUtils.hasText(authorization)) {
            ClientRequest modifiedRequest = ClientRequest.from(request)
                    .header("Authorization", authorization)
                    .build();
            return next.exchange(modifiedRequest);
        }
        return next.exchange(request);
    }
}
```

要看懂common包下McpHeaderFilter继承了filter，其中截取到了token存储在thread上下文中

### `McpHeaderFilter`**（过滤器 —— 负责"接"）**

当用户请求到达你的服务时，这个过滤器第一时间拦截请求，从 HTTP 请求头中提取 `Authorization` 字段，然后存入 `RequestHeaderContext`。

###  `RequestHeaderContext`**（上下文 —— 负责"存"）**

用 `ThreadLocal` 把 token 绑定到当前线程。这样在整个请求处理过程中，任何地方都能通过 `getAuthorization()` 拿到这个 token。最后在 `finally` 块中清除，防止线程池复用导致内存泄漏。

###  `McpHeaderConfig`**（配置 —— 负责"发"）**

自定义了一个 `WebClientCustomizer`，给所有通过 WebClient 发出的 HTTP 请求自动加上 `Authorization` 头。也就是说，当 MCP Gateway 调用下游 MCP Server 时，会自动把刚才存的 token 带上。

```txt
用户请求（带 Token）
  │
  ▼
MCP Gateway（网关 —— 中间层）
  │
  ▼
下游 MCP Server（真正干活的工具服务）
  │
  ├─ 天气 MCP Server
  ├─ 数据库 MCP Server
  └─ GitHub MCP Server
```

### 理解什么是MCP

MCP是一个协议，属于计算机网络的应用层，这个协议也分成host client server这三层

- **Host（主机）**：负责与业务代码交互，是运行大模型的应用环境（如 Claude Desktop、Cursor）。它负责接收用户请求、管理会话、编排逻辑，并调度内部的 Client。
- **Client（客户端）**：嵌入在 Host 内部，是 Host 和 Server 之间的通信中间件。它负责将 Host 的请求转换为标准 JSON-RPC 格式发给 Server，并将 Server 的响应解析后返回给 Host。一个 Host 可以包含多个 Client，每个 Client 与一个 Server 建立一对一连接。
- **Server（服务器）**：外部能力的提供者，负责实际的数据访问、API 调用或本地执行，并向 Client 暴露工具、资源和提示模板。

完整调用链路

```java
用户请求
  │
  ▼
Host（接收请求，编排逻辑）
  │
  ▼
Client（将请求转为 JSON-RPC 格式）
  │
  ▼
Server（执行工具，返回结果）
  │
  ▼
Client（接收结果，返回给 Host）
  │
  ▼
Host（整合结果，生成最终回答）
```

### 编写控制器

```java
package com.zeroone.star.ai.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.zeroone.star.project.vo.JsonVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Tag(name = "aiAssistant", description = "AI助手控制器")
public class AiAssistantController {

    //通过 @Qualifier 指定注入 Nacos 分布式同步模式的 ToolCallbackProvider（MCP 工具箱），同时注入 ChatModel（大模型）
    private final ToolCallbackProvider toolCallbackProvider;

    //
    private final ChatModel chatModel;
    
    private final ReactAgent reactAgent;

    public AiAssistantController(
            @Qualifier("distributedSyncToolCallback")
            ToolCallbackProvider toolCallbackProvider,
            ChatModel chatModel
    ) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatModel = chatModel;
        /**
         * 组装 ReactAgent：使用 Builder 模式将大模型、工具箱、系统提示词、对话记忆打包成一个完整的 AI 智能体
         * .name("mcp_aggregate_tool")：智能体名称
         * .model(chatModel)：绑定大模型作为"大脑"
         * .toolCallbackProviders(toolCallbackProvider)：绑定 MCP 工具箱作为"手脚"（Client 层）
         * .systemPrompt(...)：设定 AI 的"人设"，告诉它该做什么、不该做什么
         * .saver(new MemorySaver())：使用内存保存对话状态，支持多轮对话上下文记忆
         */
        this.reactAgent = ReactAgent.builder()
                .name("mcp_aggregate_tool")
                .model(chatModel)
                .toolCallbackProviders(toolCallbackProvider)
                .systemPrompt(
                        "你是一个有温度的MCP聚合工具，需要根据用户输入调用相应的MCP Server，返回结果；" +
                                "如果你不确定如何回答用户，就礼貌的回复用户；" +
                                "如果用户向你打招呼，你可以礼貌的回复用户我能够做什么"
                )
                //这里要使用缓存来存储状态，简化内容就使用内存保存了
                .saver(new MemorySaver())
                .build();

    }

    /**
     * 列出所有MCP聚合工具
     */
    @GetMapping("/listTool")
    @Operation(summary = "列出所有MCP聚合工具", description = "列出所有MCP聚合工具")
    public List<Map<String,String>> listsTool(){
        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        /**
         * List 自带 .stream() 方法，可以直接 list.stream()
         * 数组没有 .stream() 方法，只能借助工具类 Arrays.stream(数组) 来转换
         */
        //遍历每个工具，提取名称和描述
        /**
         * return Arrays.stream(toolCallbacks).map(new Function<ToolCallback, Map<String, String>>() {
         *             @Override
         *             public Map<String, String> apply(ToolCallback) {
         *                 return Map.of(
         *                         "name",toolCallback.getToolDefinition().name(),
         *                         "description",toolCallback.getToolDefinition().description()
         *                 );
         *             }
         *         }).collect(Collectors.toList());
         */
       return Arrays.stream(toolCallbacks).map(t -> Map.of(
               "name",t.getToolDefinition().name(),
               "description",t.getToolDefinition().description()
       )).collect(Collectors.toList());
    }

    record AgentOption(
            @Schema(description = "智能体唯一标识")
            String id,
            @Schema(description = "智能体名称")
            String name,
            @Schema(description = "智能体接口地址")
            String url,
            @Schema(description = "智能体图标")
            String icon,
            @Schema(description = "智能体描述")
            String desc
    ){
    }

    //测试智能体列表
    @GetMapping("/listAgent")
    @Operation(summary = "测试智能体列表", description = "测试智能体列表")
    public JsonVO<List<AgentOption>> listAgent(){
        return JsonVO.success(List.of(
                new AgentOption
                        ("general",
                        "通用助手",
                        "/ai/chat",
                        "\uD83E\uDDBD", "通用问答与日常协作"
                        ),
                new AgentOption
                        ("data",
                        "数据管理",
                        "/ai/data",
                        "\uD83D\uDE43",
                        "通过MCP管理数据"),
                new AgentOption
                        ("travel",
                        "旅行助手",
                        "/ai/travel",
                        "\uD83E\uDD77",
                        "旅行规划")
        ));
    }

    //通用聊天助手
    @Operation(summary = "通用聊天助手", description = "通用聊天助手")
    @PostMapping("/chat")
    public JsonVO<String> generalAssistant(String question,String context) {
        ChatClient client = ChatClient.builder(chatModel).build();
        String response = client.prompt()
                .user(question)
                .call()
                .content();
        return JsonVO.success(response);
    }
}
```

我们这样启动ai服务一定是不行的，因为还没在nacos配置服务端口和启动mcp-gateway网关服务

启动网关服务我们又会发现配置中要加载一个MCP Server此时我们还没创建先不用管,现在ai服务应该可以启动了，我们访问http://localhost:10113/ai/doc.html测试获得智能体列表的接口
![](D:\01BigProGram2026-7\his-framework\技术文档\img\屏幕截图 2026-08-26 164015.png)

和通用聊天工具的接口,测试ai是否能识别问题

![](D:\01BigProGram2026-7\his-framework\技术文档\img\屏幕截图 2026-08-26 164015.png)

以上都测试成功，需要创建MCP Server识别本项目中的sample服务的接口，首先先创建离线文档openapi,启动sample服务

![](D:\01BigProGram2026-7\his-framework\技术文档\img\屏幕截图 2026-08-26 164602.png)

创建MCP Server,导入openapi，重启mcp，ai，sample服务，即可测试ai模块是否能拿到mcp server的接口

![](D:\01BigProGram2026-7\his-framework\技术文档\img\屏幕截图 2026-08-26 164912.png)

![](D:\01BigProGram2026-7\his-framework\技术文档\img\屏幕截图 2026-08-26 165203.png)

这样我们的第一个通用助手就测试完成了，数据管理助手就可以开始搭建了,我们可以用ai智能体利用我们创建的MCP Server提供的sample接口来添加一个实例

```java
 //数据管理助手
    @Operation(summary = "数据管理助手", description = "数据管理助手")
    @PostMapping("/data")
    public JsonVO<String> dataAssistant(String question,String context) throws GraphRunnerException {
        log.info("dataAssistant question: {}, context: {}", question, context);
       //构建配置对象
        RunnableConfig config = RunnableConfig.builder().threadId(context).build();
        //调用聚合工具
        var msg = mcpAgent.call(question, config);
        log.info("dataAssistant response: {}", msg.getText());
        return JsonVO.success(msg.getText());
    }
```

![](D:\01BigProGram2026-7\his-framework\技术文档\img\屏幕截图 2026-08-26 170855.png)

A2A模块的搭建
修改项目依赖
删除了spring-cloud-starter-alibaba-nacos-discovery依赖因为不需要在nacos手动注册服务，A2A框架自动完成
添加了spring-ai-alibaba-starter-a2a-nacos

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.zeroone.star</groupId>
        <artifactId>his-java</artifactId>
        <version>${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>his-sample-agent-travel</artifactId>
    <dependencies>
        <!--spring web mvc-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--导入系统内置模块-->
        <dependency>
            <groupId>com.zeroone.star</groupId>
            <artifactId>his-common</artifactId>
        </dependency>

        <!--nacos config-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- DashScope ChatModel -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
        </dependency>
        <!-- Spring AI Alibaba Agent Framework -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-agent-framework</artifactId>
        </dependency>
        <!-- MCP Client -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-client</artifactId>
        </dependency>
        <!-- JSON Schema Validator -->
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>json-schema-validator</artifactId>
        </dependency>
        <!-- Spring AI Alibaba A2A Nacos -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter-a2a-nacos</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.zeroone.star.travel.TravelApplication</mainClass>
                </configuration>
            </plugin>
            <plugin>
                <groupId>io.fabric8</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <configuration>
                    <!-- Docker 远程管理地址-->
                    <dockerHost>https://192.168.220.128:2375</dockerHost>
                    <!-- CA 证书位置 -->
                    <certPath>/home/docker-ca</certPath>
                    <!-- 镜像设置 -->
                    <images>
                        <image>
                            <!-- Docker 镜像名称定义 -->
                            <name>01star/${project.artifactId}:${project.version}</name>
                            <!-- 指定Dockerfile所在目录 -->
                            <build>
                                <contextDir>${project.basedir}</contextDir>
                            </build>
                            <!-- 别名用于容器命名 -->
                            <alias>${project.artifactId}</alias>
                            <!-- 容器run相关配置 -->
                            <run>
                                <!-- 配置运行时容器命名策略为:别名,如果不指定则默认为none,即使用随机分配名称 -->
                                <namingStrategy>alias</namingStrategy>
                                <!-- 端口映射 -->
                                <ports>
                                    <port>10114:10114</port>
                                </ports>
                                <!-- 数据卷 -->
                                <volumes>
                                    <bind>
                                        <volume>/etc/localtime:/etc/localtime</volume>
                                        <volume>/home/app/${project.artifactId}/logs:/tmp/logs</volume>
                                    </bind>
                                </volumes>
                                <!-- 设置环境变量 -->
                                <env>
                                    <!-- JVM参数 -->
                                    <JAVA_OPTS>-Xms256m -Xmx256m</JAVA_OPTS>
                                    <!-- 启动替换参数 -->
                                    <SPRING_ARGS>
                                        --spring.profiles.active=test --spring.cloud.nacos.discovery.ip=192.168.220.128
                                    </SPRING_ARGS>
                                </env>
                            </run>
                        </image>
                    </images>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

```

application.yaml
我们要在https://modelscope.cn/上找到MCP-12306购票服务，搭建MCP购票服务，选择SSE，还要把url换成对应的url

```yaml
server:
  port: ${sp.ta}
spring:
  application:
    name: ${sn.ta}
  ai:
    # 配置DashScope模型
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          # 模型类型，如：qwen-max、qwen-turbo、qwen-plus
          model: deepseek-v4-pro
          # 核采样温度, 取值范围[0.0,1.0]，越小越模型越严谨、保守、确定, 越大模型越有创造力、越活泼、越容易天马行空
          temperature: 0.7
          # 限制大模型单次回答你时最多能吐出多少个字（1000个Token在中文里大约相当于600-800个汉字）
          max_tokens: 6000
          # 核采样, 用来过滤掉那些太离谱、太奇怪的词, 取值范围[0.0,1.0], 为1表示不限制，所有的词都参与备选，越小模型越死板严谨
          top_p: 0.9
    # AI Alibaba相关配置
    alibaba:
      # 配置A2A模型
      a2a:
        nacos:
          server-addr: ${spring.cloud.nacos.server-addr}
          username: ${spring.cloud.nacos.username}
          password: ${spring.cloud.nacos.password}
          namespace: his-dev
          registry:
            # 启用服务注册（注册本地 Agent）
            enabled: true
        server:
          version: 1.0.0
          # 服务地址, 用于服务注册时指定主机地址
          address: 192.168.9.1
          card:
            name: travel_agent
            description: 专门旅行规划的智能体
            # 提供者信息配置
            provider:
              # 提供商名称
              organization: 01星球
              # 提供商URL
              url: https://space.bilibili.com/1653229811/
    # 配置MCP
    mcp:
      client:
        type: sync
        name: a2a-mcp-client
        sse:
          connections:
            12306-mcp:
             #换成对应的url
              url: https://mcp.api-inference.modelscope.net/9c5deda2b56346/
              sse-endpoint: sse
```

导入config文件

```java
package com.zeroone.star.travel.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * <p>
 * 描述：Agent配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
@Slf4j
public class   AgentConfig {
    @Resource
    ToolCallbackProvider toolCallbackProvider;

    @Bean(name = "travelAgent")
    public ReactAgent travelAgent(ChatModel chatModel) {
        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        StringBuilder tools = new StringBuilder();
        // 提取出工具名称并用、分割拼接到toos字符串中
        for (ToolCallback toolCallback : toolCallbacks) {
            tools.append(toolCallback.getToolDefinition().name()).append("、");
        }
        // 去掉结尾的、
        if (tools.length() > 0) {
            tools.setLength(tools.length() - 1);
        }
        log.info("从SpringToolCallbackProvider获取工具 {}", tools);

        // 创建消息压缩 Hook
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(4000)
                .messagesToKeep(2)
                .build();

        return ReactAgent.builder()
                .name("travel_agent")
                .model(chatModel)
                .description("专门旅行规划的智能体")
                .toolCallbackProviders(toolCallbackProvider)
                .hooks(summarizationHook)
                .instruction("""
                        你是一个专业的旅行规划智能助手。你的主要任务是帮助用户制定详细、个性化且可行的旅行计划。
                        重要：回答内容统一使用以 "好的，下面是我给您的规划建议:"  开头

                        请遵循以下原则：
                        1. 需求分析：首先明确用户的目的地、出行时间、预算范围、同行人员（如家庭、情侣、独自旅行）以及兴趣偏好（如自然风光、历史文化、美食购物、冒险活动等）。
                        2. 行程规划：提供按天划分的详细行程安排，包括上午、下午和晚上的活动建议。
                        3. 实用信息：在推荐景点或活动时，提供必要的实用信息，如预计耗时、门票价格、最佳游览时间、交通方式建议等。
                        4. 住宿餐饮：根据用户的预算和偏好，推荐合适的住宿区域或具体酒店类型，以及当地特色美食或餐厅建议。
                        5. 注意事项：提醒用户目的地的天气状况、穿衣建议、签证要求、安全提示及当地风俗习惯。
                        6. 交通规划：根据用户出发地规划出行火车票信息。
                        7. 如果用户向你打招呼，你就礼貌的回答你能做什么。

                        请以友好、专业且富有启发性的语气与用户交流，确保旅行计划既令人兴奋又切实可行。
                        """
                )
                .saver(new MemorySaver())
                .build();
    }
}
```

完善好控制器(删除了通用和数据助手方便看旅行助手实现)

```java
package com.zeroone.star.ai.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.zeroone.star.project.vo.JsonVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 旅行助手控制器
 * 专注于处理旅行规划相关的 A2A 远程调用
 */
@RestController
@RequestMapping("/ai") // 建议统一加个前缀，保持路径整洁
@Tag(name = "AI旅行助手", description = "提供基于 A2A 协议的旅行规划服务")
@Slf4j
public class AiAssistantController {

    /**
     * 旅行智能体 (A2A Remote Agent)
     * 通过 Nacos 发现远程服务并进行调用
     */
    private final Agent travelAgent;

    /**
     * 构造器注入
     * @param agentCardProvider Nacos 服务发现提供者，用于定位远程 travel_agent
     */
    public AiAssistantController(
            @Qualifier("nacosAgentCardProvider") AgentCardProvider agentCardProvider
    ) {
        // 构建远程旅行助手 Agent
        this.travelAgent = A2aRemoteAgent.builder()
                .name("travel_agent")
                .description("旅行助手")
                .instruction("你是一个旅行助手，需要根据用户输入提供旅行相关的信息 {input}")
                .agentCardProvider(agentCardProvider)
                .outputKey("travel_planning")
                .shareState(true)
                .build();

        log.info("AiAssistantController 初始化完成，已加载 travelAgent");
    }

    /**
     * 旅行规划接口
     *
     * @param question 用户的问题或需求
     * @param context  会话上下文 ID (用于多轮对话记忆)
     * @return 旅行规划建议
     */
    @PostMapping("/travel")
    @Operation(summary = "旅行助手", description = "调用远程 Travel Agent 进行行程规划")
    public JsonVO<String> travelAssistant(String question, String context) throws GraphRunnerException {
        log.info("收到旅行规划请求 | question: {} | context: {}", question, context);

        // 1. 构建运行配置 (绑定会话 ID)
        RunnableConfig config = RunnableConfig.builder()
                .threadId(context)
                .build();

        // 2. 调用远程 Agent
        var state = travelAgent.invoke(question, config);

        // 3. 处理结果
        if (state.isEmpty()) {
            log.warn("旅行助手返回结果为空");
            return JsonVO.fail("旅行助手未找到相关结果");
        }

        // 4. 提取指定 Key 的输出内容
        String answerStr = state.get().value("travel_planning", "暂时未找到相关结果");
        log.info("旅行规划生成完毕 | length: {}", answerStr.length());

        return JsonVO.success(answerStr);
    }
}
```

启动四个有关模块