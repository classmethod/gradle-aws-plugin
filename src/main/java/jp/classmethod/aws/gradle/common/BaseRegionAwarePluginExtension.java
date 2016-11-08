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

import jp.classmethod.aws.gradle.AwsPluginExtension;

public class BaseRegionAwarePluginExtension<T extends AmazonWebServiceClient>extends BasePluginExtension<T> {
	
	@Getter
	@Setter
	private String region;
	
	
	public BaseRegionAwarePluginExtension(Project project, Class<T> awsClientClass) {
		super(project, awsClientClass);
	}
	
	@Override
	protected T initClient() {
		AwsPluginExtension aws = getProject().getExtensions().getByType(AwsPluginExtension.class);
		T client = super.initClient();
		if (isRegionRequired() || region != null) {
			client.setRegion(aws.getActiveRegion(region));
		}
		
		return client;
	}
	
	/**
	 * Most clients require a region to be set, but a few allow it to be optional.
	 * For optional clients, subclasses should override and return false.
	 *
	 * @return true if region is required (default), else false.
	 */
	protected boolean isRegionRequired() {
		return true;
	}
	
}
