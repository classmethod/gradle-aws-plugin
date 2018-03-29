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
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;

import jp.classmethod.aws.gradle.AwsPluginExtension;

public abstract class BasePluginExtension<T extends AmazonWebServiceClient> {
	
	private final Class<T> awsClientClass;
	
	@Getter
	@Setter
	private Project project;
	
	@Getter
	@Setter
	private String profileName;
	
	private T client;
	
	
	@SuppressWarnings("unchecked")
	public T getClient() {
		if (client == null) {
			client = initClient();
		}
		return client;
	}
	
	public BasePluginExtension(Project project, Class<T> awsClientClass) {
		this.project = project;
		this.awsClientClass = awsClientClass;
	}
	
	protected T initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(awsClientClass, profileName, buildClientConfiguration());
	}
	
	/**
	 * Allow subclasses to build a custom client configuration.
	 *
	 * @return  AWS ClientConfiguration
	 */
	protected ClientConfiguration buildClientConfiguration() { // NOPMD
		return null;
	}
}
