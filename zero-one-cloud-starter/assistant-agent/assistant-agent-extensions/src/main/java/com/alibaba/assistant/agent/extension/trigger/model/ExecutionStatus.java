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

package com.alibaba.assistant.agent.extension.trigger.model;

/**
 * 执行状态枚举
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum ExecutionStatus {

	/**
	 * 待执行状态（已排队但未开始）
	 */
	PENDING,

	/**
	 * 执行中状态
	 */
	RUNNING,

	/**
	 * 执行成功状态
	 */
	SUCCESS,

	/**
	 * 执行失败状态
	 */
	FAILED,

	/**
	 * 跳过执行状态（条件不满足）
	 */
	SKIPPED,

	/**
	 * 执行超时状态
	 */
	TIMEOUT

}

