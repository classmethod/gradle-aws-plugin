/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.common;

import org.gradle.api.internal.ConventionTask;

import jp.classmethod.aws.gradle.AwsPluginExtension;

/**
 * TODO daisuke.
 *
 * @author daisuke
 * @since #version#
 */
public abstract class BaseAwsTask extends ConventionTask {
	
	public BaseAwsTask(String group, String description) {
		setGroup(group);
		setDescription(description);
	}
	
	public <T> T getPluginExtension(Class<T> type) {
		return getProject().getExtensions().getByType(AwsPluginExtension.class)
			.asExtensionAware().getExtensions().getByType(type);
	}
}
