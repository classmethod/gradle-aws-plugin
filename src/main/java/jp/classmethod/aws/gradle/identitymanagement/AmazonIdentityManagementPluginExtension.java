/*
 * Copyright 2013-2016 Classmethod, Inc.
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

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;


public class AmazonIdentityManagementPluginExtension {
	
	public static final String NAME = "iam";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter(lazy = true)
	private final AmazonIdentityManagement client = initClient();

	public AmazonIdentityManagementPluginExtension(Project project) {
		this.project = project;
	}

	private AmazonIdentityManagement initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AmazonIdentityManagement client = aws.createClient(AmazonIdentityManagementClient.class, profileName);
		return client;
	}
}
