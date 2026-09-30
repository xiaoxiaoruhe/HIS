package com.alibaba.assistant.agent.extension.experience.fastintent;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FastIntentContext - 统一条件判断上下文
 */
public class FastIntentContext {

    private final String input;

    private final List<Message> messages;

    private final Map<String, Object> configMetadata;

    private final OverAllState state;

    /**
     * Tool request (for CODE fastpath), e.g. {requirement, functionName, parameters}
     */
    private final Map<String, Object> toolRequest;

    public FastIntentContext(String input,
                             List<Message> messages,
                             Map<String, Object> configMetadata,
                             OverAllState state,
                             Map<String, Object> toolRequest) {
        this.input = input;
        this.messages = messages != null ? messages : List.of();
        this.configMetadata = configMetadata != null ? configMetadata : Collections.emptyMap();
        this.state = state;
        this.toolRequest = toolRequest != null ? toolRequest : Collections.emptyMap();
    }

    public String getInput() {
        return input;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Map<String, Object> getConfigMetadata() {
        return configMetadata;
    }

    public OverAllState getState() {
        return state;
    }

    public Map<String, Object> getToolRequest() {
        return toolRequest;
    }

    public Optional<Object> stateValue(String key) {
        if (state == null || !StringUtils.hasText(key)) {
            return Optional.empty();
        }
        return state.value(key);
    }

    public static FastIntentContext from(OverAllState state, RunnableConfig config) {
        String input = null;
        if (state != null) {
            input = state.value("input", String.class).orElse(null);
        }
        Map<String, Object> md = config != null ? config.metadata().orElse(Collections.emptyMap()) : Collections.emptyMap();
        @SuppressWarnings("unchecked")
        List<Message> msgs = state != null ? state.value("messages", List.class).orElse(List.of()) : List.of();
        return new FastIntentContext(input, msgs, md, state, null);
    }
}
