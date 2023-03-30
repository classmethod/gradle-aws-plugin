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
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;

public class AmazonIdentityManagementCreateRoleTask extends ConventionTask {
	
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
		setDescription("Create Role.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void createRole() {
		// to enable conventionMappings feature
		checkRoleParams();
		AmazonIdentityManagementPluginExtension ext =
				getProject().getExtensions().getByType(AmazonIdentityManagementPluginExtension.class);
		AmazonIdentityManagement iam = ext.getClient();
		createRole = createIAMRole(iam);
		getLogger().info("Create Role requested: {}", createRole.getRole().getArn());
		attachPoliciesToRole(iam);
	}
	
	@TaskAction
	private void checkRoleParams() { // NOPMD
		// to enable conventionMappings feature
		if (getRoleName() == null) {
			throw new GradleException("roleName is required");
		}
		if (getAssumeRolePolicyDocument() == null) {
			throw new GradleException("assumeRolePolicyDocument is required");
		}
	}
	
	@TaskAction
	private CreateRoleResult createIAMRole(AmazonIdentityManagement iam) { // NOPMD
		// to enable conventionMappings feature
		CreateRoleRequest request = new CreateRoleRequest()
			.withRoleName(getRoleName())
			.withPath(getPath())
			.withAssumeRolePolicyDocument(getAssumeRolePolicyDocument());
		return iam.createRole(request);
	}
	
	@TaskAction
	private void attachPoliciesToRole(AmazonIdentityManagement iam) { // NOPMD
		// to enable conventionMappings feature
		policyArns.stream().forEach(policyArn -> {
			iam.attachRolePolicy(new AttachRolePolicyRequest()
				.withRoleName(getRoleName())
				.withPolicyArn(policyArn));
			getLogger().info("Attach Managed policy {} to Role {} requested", policyArn, getRoleName());
		});
	}
}
