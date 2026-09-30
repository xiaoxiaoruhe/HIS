package com.zeroone.star.ai.controller;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.zeroone.star.project.vo.JsonVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Tag(name = "AI助手控制器", description = "AI助手控制器")
@Slf4j
public class AiAssistantController {

    //通过 @Qualifier 指定注入 Nacos 分布式同步模式的 ToolCallbackProvider（MCP 工具箱），同时注入 ChatModel（大模型）
    private final ToolCallbackProvider toolCallbackProvider;

    //
    private final ChatModel chatModel;
    
    private final ReactAgent mcpAgent;

    private final Agent travelAgent;

    public AiAssistantController(
            @Qualifier("distributedSyncToolCallback")
            ToolCallbackProvider toolCallbackProvider,
            ChatModel chatModel,
            @Qualifier("nacosAgentCardProvider") AgentCardProvider agentCardProvider
    ) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatModel = chatModel;
        /*
          组装 ReactAgent：使用 Builder 模式将大模型、工具箱、系统提示词、对话记忆打包成一个完整的 AI 智能体
          .name("mcp_aggregate_tool")：智能体名称
          .model(chatModel)：绑定大模型作为"大脑"
          .toolCallbackProviders(toolCallbackProvider)：绑定 MCP 工具箱作为"手脚"（Client 层）
          .systemPrompt(...)：设定 AI 的"人设"，告诉它该做什么、不该做什么
          .saver(new MemorySaver())：使用内存保存对话状态，支持多轮对话上下文记忆
         */
        this.mcpAgent = ReactAgent.builder()
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

        this.travelAgent = A2aRemoteAgent.builder()
                .name("travel_agent")
                .description("旅行助手")
                .instruction("你是一个旅行助手，需要根据用户输入提供旅行相关的信息" +
                        "{input}")
                .agentCardProvider(agentCardProvider)
                .outputKey("travel_planning")
                .shareState(true)
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


    //旅行助手
    @Operation(summary = "旅行助手", description = "旅行助手")
    @PostMapping("/travel")
    public JsonVO<String> travelAssistant(String question,String context) throws GraphRunnerException {
        log.info("travelAssistant question: {}, context: {}", question, context);
        //构建配置对象
        RunnableConfig config = RunnableConfig.builder().threadId(context).build();
        //调用agent
        var state = travelAgent.invoke(question, config);
        if (state.isEmpty()){
            return JsonVO.fail("旅行助手未找到相关结果");
        }
        //获取结果
        String answerStr = state.get().value("travel_planning", "暂时未找到相关结果");
        log.info("travelAssistant response: {}", answerStr);
        return JsonVO.success(answerStr);
    }
}
