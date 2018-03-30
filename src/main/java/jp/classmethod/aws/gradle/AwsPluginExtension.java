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
package jp.classmethod.aws.gradle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

import groovy.lang.Closure;

@RequiredArgsConstructor
public class AwsPluginExtension {
	
	public static final String NAME = "aws";
	
	@Getter
	private final Project project;
	
	@Getter
	@Setter
	private Closure<?> clientBuilderConfig;
	
	
	public String getAccountId() {
		return getGetCallerIdentityResult().getAccount();
	}
	
	public String getUserArn() {
		return getGetCallerIdentityResult().getArn();
	}
	
	private GetCallerIdentityResult getGetCallerIdentityResult() {
		AWSSecurityTokenServiceClientBuilder builder = AWSSecurityTokenServiceClientBuilder.standard();
		if (clientBuilderConfig != null) {
			clientBuilderConfig.setDelegate(builder);
			clientBuilderConfig.call();
		}
		return builder.build().getCallerIdentity(new GetCallerIdentityRequest());
	}
	
	public ExtensionAware asExtensionAware() {
		if (this instanceof ExtensionAware) {
			return (ExtensionAware) this;
		}
		throw new AssertionError("Extension does not implement ExtensionAware");
	}
}
