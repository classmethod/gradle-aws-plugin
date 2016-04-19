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
package jp.classmethod.aws.gradle.cloudformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;


public class AmazonCloudFormationCreateChangeSetTask extends ConventionTask {
	
	@Getter @Setter
	private String stackName;
	
	@Getter @Setter
	private String cfnTemplateUrl;
	
	@Getter @Setter
	private List<Parameter> cfnStackParams = new ArrayList<>();
	
	@Getter @Setter
	private boolean capabilityIam;
	
	@Getter @Setter
	private List<String> stableStatuses = Arrays.asList(
		"CREATE_COMPLETE", "ROLLBACK_COMPLETE", "UPDATE_COMPLETE", "UPDATE_ROLLBACK_COMPLETE"
	);
	
	public AmazonCloudFormationCreateChangeSetTask() {
		setDescription("Create cfn change set.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void creatChangeSet() throws InterruptedException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		List<String> stableStatuses = getStableStatuses();
		
		if (stackName == null) throw new GradleException("stackName is not specified");
		if (cfnTemplateUrl == null) throw new GradleException("cfnTemplateUrl is not specified");
		
		AmazonCloudFormationPluginExtension ext = getProject().getExtensions().getByType(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		DescribeStacksResult describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName));
		Stack stack = describeStackResult.getStacks().get(0);
		if (stableStatuses.contains(stack.getStackStatus())) {
			createChangeSet(cfn);
		} else {
			throw new GradleException("invalid status for create change set: " + stack.getStackStatus());
		}
	}
	
	private void createChangeSet(AmazonCloudFormation cfn) {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		List<Parameter> cfnStackParams = getCfnStackParams();
		
		String changeSetName = changeSetName(stackName);
		getLogger().info("Create change set '{}' for stack '{}'", changeSetName, stackName);
		CreateChangeSetRequest req = new CreateChangeSetRequest()
			.withChangeSetName(changeSetName)
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams);
		if (isCapabilityIam()) {
			req.setCapabilities(Arrays.asList(Capability.CAPABILITY_IAM.toString()));
		}
		CreateChangeSetResult createChangeSetResult = cfn.createChangeSet(req);
		getLogger().info("Create change set requested: {}", createChangeSetResult.getId());
	}

	private String changeSetName(String stackName) {
		return stackName + new SimpleDateFormat("-yyyyMMdd-HHmmss").format(new Date());
	}
}
