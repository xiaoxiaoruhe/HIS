package com.alibaba.assistant.agent.extension.experience.fastintent;

import com.alibaba.assistant.agent.extension.experience.model.FastIntentConfig;

/**
 * SPI: register custom condition matchers by type.
 */
public interface FastIntentConditionMatcher {

    /**
     * Condition type string, e.g. "message_prefix"
     */
    String getType();

    boolean matches(FastIntentConfig.Condition condition, FastIntentContext context);
}


