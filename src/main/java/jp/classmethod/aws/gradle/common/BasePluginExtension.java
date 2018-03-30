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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.client.builder.AwsSyncClientBuilder;

import jp.classmethod.aws.gradle.AwsPluginExtension;

import groovy.lang.Closure;

@RequiredArgsConstructor
public abstract class BasePluginExtension<T> {
	
	@Getter
	private final Project project;
	
	private final AwsSyncClientBuilder<?, T> builder;
	
	@Getter
	@Setter
	private Closure<?> clientBuilderConfig;
	
	private T client;
	
	
	@SuppressWarnings("unchecked")
	public T getClient() {
		if (client == null) {
			configureBuilder(builder);
			client = builder.build();
		}
		return client;
	}
	
	/**
	 * Allow subclasses to build a custom client configuration.
	 */
	protected void configureBuilder(AwsSyncClientBuilder<?, T> builder) { // NOPMD
		Closure<?> commonConfig = project.getExtensions().getByType(AwsPluginExtension.class).getClientBuilderConfig();
		if (commonConfig != null) {
			commonConfig.setDelegate(builder);
			commonConfig.call();
		}
		if (clientBuilderConfig != null) {
			clientBuilderConfig.setDelegate(builder);
			clientBuilderConfig.call();
		}
	}
}
