package com.alibaba.assistant.agent.extension.experience.model;

import java.util.ArrayList;
import java.util.List;

/**
 * FastIntentConfig - 每条经验的 FastPath Intent 配置
 */
public class FastIntentConfig {

    private boolean enabled = false;

    /**
     * Higher wins when multiple experiences match
     */
    private int priority = 0;

    private MatchExpression match;

    private OnMatch onMatch = new OnMatch();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public MatchExpression getMatch() {
        return match;
    }

    public void setMatch(MatchExpression match) {
        this.match = match;
    }

    public OnMatch getOnMatch() {
        return onMatch;
    }

    public void setOnMatch(OnMatch onMatch) {
        this.onMatch = onMatch != null ? onMatch : new OnMatch();
    }

    public static class OnMatch {

        private FastIntentMode mode = FastIntentMode.FASTPATH;

        private FastIntentFallback fallback = FastIntentFallback.REFERENCE_ONLY;

        public FastIntentMode getMode() {
            return mode;
        }

        public void setMode(FastIntentMode mode) {
            this.mode = mode != null ? mode : FastIntentMode.FASTPATH;
        }

        public FastIntentFallback getFallback() {
            return fallback;
        }

        public void setFallback(FastIntentFallback fallback) {
            this.fallback = fallback != null ? fallback : FastIntentFallback.REFERENCE_ONLY;
        }
    }

    public enum FastIntentMode {
        FASTPATH,
        FASTPATH_THEN_REFERENCE
    }

    public enum FastIntentFallback {
        REFERENCE_ONLY,
        FAIL_FAST
    }

    /**
     * JSON-friendly match expression:
     * - anyOf: OR
     * - allOf: AND
     * - not: NOT
     * - condition: atom
     */
    public static class MatchExpression {

        private List<MatchExpression> anyOf = new ArrayList<>();

        private List<MatchExpression> allOf = new ArrayList<>();

        private MatchExpression not;

        private Condition condition;

        public List<MatchExpression> getAnyOf() {
            return anyOf;
        }

        public void setAnyOf(List<MatchExpression> anyOf) {
            this.anyOf = anyOf != null ? anyOf : new ArrayList<>();
        }

        public List<MatchExpression> getAllOf() {
            return allOf;
        }

        public void setAllOf(List<MatchExpression> allOf) {
            this.allOf = allOf != null ? allOf : new ArrayList<>();
        }

        public MatchExpression getNot() {
            return not;
        }

        public void setNot(MatchExpression not) {
            this.not = not;
        }

        public Condition getCondition() {
            return condition;
        }

        public void setCondition(Condition condition) {
            this.condition = condition;
        }
    }

    public static class Condition {

        /**
         * e.g. message_prefix / message_regex / metadata_equals ...
         */
        private String type;

        private String key;

        private String value;

        private List<String> values = new ArrayList<>();

        private String pattern;

        private Boolean ignoreCase;

        private Boolean trim;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values != null ? values : new ArrayList<>();
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Boolean getIgnoreCase() {
            return ignoreCase;
        }

        public void setIgnoreCase(Boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        public Boolean getTrim() {
            return trim;
        }

        public void setTrim(Boolean trim) {
            this.trim = trim;
        }
    }
}


