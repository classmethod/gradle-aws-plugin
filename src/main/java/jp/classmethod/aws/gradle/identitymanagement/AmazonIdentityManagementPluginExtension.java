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
package jp.classmethod.aws.gradle.identitymanagement;

import org.gradle.api.Project;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;

import jp.classmethod.aws.gradle.common.BasePluginExtension;

public class AmazonIdentityManagementPluginExtension extends BasePluginExtension<AmazonIdentityManagement> {
	
	public static final String NAME = "iam";
	
	
	public AmazonIdentityManagementPluginExtension(Project project) {
		super(project, AmazonIdentityManagementClientBuilder.standard());
	}
	
}
