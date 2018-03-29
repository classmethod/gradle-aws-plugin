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
package jp.classmethod.aws.gradle.cloudformation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.services.cloudformation.model.UpdateStackResult;
import com.google.common.base.Strings;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonCloudFormationMigrateStackTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String stackName;
	
	@Getter
	@Setter
	private String cfnTemplateUrl;
	
	@Getter
	@Setter
	private File cfnTemplateFile;
	
	@Getter
	@Setter
	private List<Parameter> cfnStackParams = new ArrayList<>();
	
	@Getter
	@Setter
	private List<Tag> cfnStackTags = new ArrayList<>();
	
	@Getter
	@Setter
	private boolean capabilityIam;
	
	@Getter
	@Setter
	private Capability useCapabilityIam;
	
	@Getter
	@Setter
	private String cfnStackPolicyUrl;
	
	@Getter
	@Setter
	private File cfnStackPolicyFile;
	
	@Getter
	@Setter
	private String cfnOnFailure;
	
	@Getter
	@Setter
	private List<String> stableStatuses = Arrays.asList(
			"CREATE_COMPLETE", "ROLLBACK_COMPLETE", "UPDATE_COMPLETE", "UPDATE_ROLLBACK_COMPLETE");
	
	
	public AmazonCloudFormationMigrateStackTask() {
		super("AWS", "Create / Migrate cfn stack.");
	}
	
	@TaskAction
	public void createOrUpdateStack() throws InterruptedException, IOException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		File cfnTemplateFile = getCfnTemplateFile();
		List<String> stableStatuses = getStableStatuses();
		
		if (stackName == null) {
			throw new GradleException("stackName is not specified");
		}
		
		AmazonCloudFormationPluginExtension ext = getPluginExtension(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		try {
			DescribeStacksResult describeStackResult =
					cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName));
			Stack stack = describeStackResult.getStacks().get(0);
			if (stack.getStackStatus().equals("DELETE_COMPLETE")) {
				getLogger().warn("deleted stack {} already exists", stackName);
				deleteStack(cfn);
				createStack(cfn);
			} else if (stableStatuses.contains(stack.getStackStatus())) {
				updateStack(cfn);
			} else {
				throw new GradleException("invalid status for update: " + stack.getStackStatus());
			}
		} catch (AmazonServiceException e) {
			if (e.getMessage().contains("does not exist")) {
				getLogger().warn("stack {} not found", stackName);
				if (cfnTemplateUrl == null && cfnTemplateFile == null) {
					getLogger().error("cfnTemplateUrl or cfnTemplateFile must be provided");
					throw e;
				}
				createStack(cfn);
			} else if (e.getMessage().contains("No updates are to be performed.")) {
				getLogger().trace(e.getMessage());
			} else {
				throw e;
			}
		}
	}
	
	private void updateStack(AmazonCloudFormation cfn) throws IOException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		File cfnTemplateFile = getCfnTemplateFile();
		List<Parameter> cfnStackParams = getCfnStackParams();
		List<Tag> cfnStackTags = getCfnStackTags();
		String cfnStackPolicyUrl = getCfnStackPolicyUrl();
		File cfnStackPolicyFile = getCfnStackPolicyFile();
		
		getLogger().info("Update stack: {}", stackName);
		
		getLogger().info("==== Parameters ====");
		cfnStackParams.stream()
			.forEach(p -> {
				getLogger().info("{} = {}",
						p.getParameterKey(),
						p.getParameterValue());
			});
		
		UpdateStackRequest req = new UpdateStackRequest()
			.withStackName(stackName)
			.withParameters(cfnStackParams)
			.withTags(cfnStackTags);
		
		// If template URL is specified, then use it
		if (Strings.isNullOrEmpty(cfnTemplateUrl) == false) {
			req.setTemplateURL(cfnTemplateUrl);
			getLogger().info("Using template url: {}", cfnTemplateUrl);
			// Else, use the template file body
		} else if (cfnStackPolicyFile != null) {
			req.setTemplateBody(FileUtils.readFileToString(cfnTemplateFile));
			getLogger().info("Using template file: {}", "$cfnTemplateFile.canonicalPath");
		} else {
			req.setUsePreviousTemplate(true);
			getLogger().info("No template specified, updating existing template");
		}
		if (isCapabilityIam()) {
			Capability selectedCapability =
					(getUseCapabilityIam() == null) ? Capability.CAPABILITY_IAM : getUseCapabilityIam();
			getLogger().info("Using IAM capability: " + selectedCapability);
			req.setCapabilities(Arrays.asList(selectedCapability.toString()));
		}
		
		// If stack policy is specified, then use it
		if (Strings.isNullOrEmpty(cfnStackPolicyUrl) == false) {
			req.setStackPolicyURL(cfnStackPolicyUrl);
			// Else, use the stack policy file body if present
		} else if (cfnStackPolicyFile != null) {
			req.setStackPolicyBody(
					FileUtils.readFileToString(cfnStackPolicyFile));
		}
		
		UpdateStackResult updateStackResult = cfn.updateStack(req);
		getLogger().info("Update requested: {}", updateStackResult.getStackId());
	}
	
	private void deleteStack(AmazonCloudFormation cfn) throws InterruptedException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		
		getLogger().info("delete stack: {}", stackName);
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName));
		getLogger().info("delete requested: {}", stackName);
		Thread.sleep(3000);
	}
	
	private void createStack(AmazonCloudFormation cfn) throws IOException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		File cfnTemplateFile = getCfnTemplateFile();
		List<Parameter> cfnStackParams = getCfnStackParams();
		List<Tag> cfnStackTags = getCfnStackTags();
		String cfnStackPolicyUrl = getCfnStackPolicyUrl();
		File cfnStackPolicyFile = getCfnStackPolicyFile();
		String cfnOnFailure = getCfnOnFailure();
		
		getLogger().info("create stack: {}", stackName);
		
		getLogger().info("==== Parameters ====");
		cfnStackParams.stream()
			.forEach(p -> {
				getLogger().info("{} = {}",
						p.getParameterKey(),
						p.getParameterValue());
			});
		
		CreateStackRequest req = new CreateStackRequest()
			.withStackName(stackName)
			.withParameters(cfnStackParams)
			.withTags(cfnStackTags)
			.withOnFailure(cfnOnFailure);
		
		// If template URL is specified, then use it
		if (Strings.isNullOrEmpty(cfnTemplateUrl) == false) {
			req.setTemplateURL(cfnTemplateUrl);
			// Else, use the template file body
		} else {
			req.setTemplateBody(FileUtils.readFileToString(cfnTemplateFile));
		}
		if (isCapabilityIam()) {
			Capability selectedCapability =
					(getUseCapabilityIam() == null) ? Capability.CAPABILITY_IAM : getUseCapabilityIam();
			getLogger().info("Using IAM capability: " + selectedCapability);
			req.setCapabilities(Arrays.asList(selectedCapability.toString()));
		}
		
		// If stack policy is specified, then use it
		if (Strings.isNullOrEmpty(cfnStackPolicyUrl) == false) {
			req.setStackPolicyURL(cfnStackPolicyUrl);
			// Else, use the stack policy file body
		} else if (cfnStackPolicyFile != null) {
			req.setStackPolicyBody(
					FileUtils.readFileToString(cfnStackPolicyFile));
		}
		
		CreateStackResult createStackResult = cfn.createStack(req);
		getLogger().info("create requested: {}", createStackResult.getStackId());
	}
}
