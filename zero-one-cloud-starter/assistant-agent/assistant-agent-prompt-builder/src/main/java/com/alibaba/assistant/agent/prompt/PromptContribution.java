/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.assistant.agent.prompt;

import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A contribution produced by prompt builders.
 * <p>
 * Note: In this project, {@code AgentLlmNode} prepends {@link org.springframework.ai.chat.messages.SystemMessage} (from
 * {@code ModelRequest#getSystemMessage()}) into the outgoing message list. To avoid
 * multiple {@link org.springframework.ai.chat.messages.SystemMessage} instances, prefer contributing system text via
 * {@link #systemTextToPrepend()} / {@link #systemTextToAppend()} instead of placing
 * {@link org.springframework.ai.chat.messages.SystemMessage} into {@link #messagesToPrepend()} / {@link #messagesToAppend()}.
 */
public final class PromptContribution {

	private final String systemTextToPrepend;

	private final String systemTextToAppend;

	private final List<Message> messagesToPrepend;

	private final List<Message> messagesToAppend;

	private PromptContribution(Builder builder) {
		this.systemTextToPrepend = builder.systemTextToPrepend;
		this.systemTextToAppend = builder.systemTextToAppend;
		this.messagesToPrepend = List.copyOf(builder.messagesToPrepend);
		this.messagesToAppend = List.copyOf(builder.messagesToAppend);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static PromptContribution empty() {
		return builder().build();
	}

	public String systemTextToPrepend() {
		return systemTextToPrepend;
	}

	public String systemTextToAppend() {
		return systemTextToAppend;
	}

	public List<Message> messagesToPrepend() {
		return Collections.unmodifiableList(messagesToPrepend);
	}

	public List<Message> messagesToAppend() {
		return Collections.unmodifiableList(messagesToAppend);
	}

	public boolean isEmpty() {
		return (systemTextToPrepend == null || systemTextToPrepend.isBlank())
				&& (systemTextToAppend == null || systemTextToAppend.isBlank()) && messagesToPrepend.isEmpty()
				&& messagesToAppend.isEmpty();
	}

	public static final class Builder {

		private String systemTextToPrepend;

		private String systemTextToAppend;

		private final List<Message> messagesToPrepend = new ArrayList<>();

		private final List<Message> messagesToAppend = new ArrayList<>();

		public Builder systemTextToPrepend(String text) {
			this.systemTextToPrepend = text;
			return this;
		}

		public Builder systemTextToAppend(String text) {
			this.systemTextToAppend = text;
			return this;
		}

		public Builder prepend(Message message) {
			this.messagesToPrepend.add(Objects.requireNonNull(message, "message must not be null"));
			return this;
		}

		public Builder prependAll(List<? extends Message> messages) {
			if (messages != null) {
				messages.forEach(this::prepend);
			}
			return this;
		}

		public Builder append(Message message) {
			this.messagesToAppend.add(Objects.requireNonNull(message, "message must not be null"));
			return this;
		}

		public Builder appendAll(List<? extends Message> messages) {
			if (messages != null) {
				messages.forEach(this::append);
			}
			return this;
		}

		public PromptContribution build() {
			return new PromptContribution(this);
		}

	}

}


