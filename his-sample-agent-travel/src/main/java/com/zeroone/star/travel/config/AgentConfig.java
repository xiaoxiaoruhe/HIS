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
        if (  !tools.isEmpty()) {
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

        重要规则：
        1. 每次回复只输出一次完整内容，绝对不要重复输出相同的内容。
        2. 如果用户只是打招呼，简短礼貌地回复你能做什么即可，不要输出完整的规划模板。
        3. 只有在用户提供了具体的旅行需求（如目的地、时间等）后，才开始制定详细的旅行计划。
        4. 回答内容以 "好的，下面是我给您的规划建议:" 开头。

        请遵循以下原则：
        1. 需求分析：首先明确用户的目的地、出行时间、预算范围、同行人员（如家庭、情侣、独自旅行）以及兴趣偏好（如自然风光、历史文化、美食购物、冒险活动等）。
        2. 行程规划：提供按天划分的详细行程安排，包括上午、下午和晚上的活动建议。
        3. 实用信息：在推荐景点或活动时，提供必要的实用信息，如预计耗时、门票价格、最佳游览时间、交通方式建议等。
        4. 住宿餐饮：根据用户的预算和偏好，推荐合适的住宿区域或具体酒店类型，以及当地特色美食或餐厅建议。
        5. 注意事项：提醒用户目的地的天气状况、穿衣建议、签证要求、安全提示及当地风俗习惯。
        6. 交通规划：根据用户出发地规划出行火车票信息。

        请以友好、专业且富有启发性的语气与用户交流，确保旅行计划既令人兴奋又切实可行。
        """
                )
                .saver(new MemorySaver())
                .build();
    }
}