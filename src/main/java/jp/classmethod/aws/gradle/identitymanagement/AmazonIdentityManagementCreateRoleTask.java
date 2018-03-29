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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonIdentityManagementCreateRoleTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String path = "/";
	
	@Getter
	@Setter
	private String roleName;
	
	@Getter
	@Setter
	private String assumeRolePolicyDocument;
	
	@Getter
	@Setter
	private List<String> policyArns = new ArrayList<>();
	
	@Getter
	private CreateRoleResult createRole;
	
	
	public AmazonIdentityManagementCreateRoleTask() {
		super("AWS", "Create Role.");
	}
	
	@TaskAction
	public void createRole() {
		// to enable conventionMappings feature
		String roleName = getRoleName();
		String assumeRolePolicyDocument = getAssumeRolePolicyDocument();
		
		if (roleName == null) {
			throw new GradleException("roleName is required");
		}
		if (assumeRolePolicyDocument == null) {
			throw new GradleException("assumeRolePolicyDocument is required");
		}
		
		AmazonIdentityManagementPluginExtension ext = getPluginExtension(AmazonIdentityManagementPluginExtension.class);
		AmazonIdentityManagement iam = ext.getClient();
		
		CreateRoleRequest request = new CreateRoleRequest()
			.withRoleName(roleName)
			.withPath(getPath())
			.withAssumeRolePolicyDocument(assumeRolePolicyDocument);
		createRole = iam.createRole(request);
		getLogger().info("Create Role requested: {}", createRole.getRole().getArn());
		policyArns.forEach(policyArn -> {
			iam.attachRolePolicy(new AttachRolePolicyRequest()
				.withRoleName(roleName)
				.withPolicyArn(policyArn));
			getLogger().info("Attach Managed policy {} to Role {} requested", policyArn, roleName);
		});
	}
}
