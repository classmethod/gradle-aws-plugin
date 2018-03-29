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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.ChangeSetSummary;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.ListChangeSetsRequest;
import com.amazonaws.services.cloudformation.model.ListChangeSetsResult;
import com.amazonaws.services.cloudformation.model.Stack;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonCloudFormationExecuteChangeSetTask extends BaseAwsTask {
	
	@Getter
	@Setter
	String stackName;
	
	private List<String> stableStatuses = Arrays.asList(
			"CREATE_COMPLETE", "ROLLBACK_COMPLETE", "UPDATE_COMPLETE", "UPDATE_ROLLBACK_COMPLETE");
	
	
	public AmazonCloudFormationExecuteChangeSetTask() {
		super("AWS", "Execute the latest cfn change set.");
	}
	
	@TaskAction
	public void executeChangeSet() throws InterruptedException, IOException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		
		if (stackName == null) {
			throw new GradleException("stackName is not specified");
		}
		
		AmazonCloudFormationPluginExtension ext = getPluginExtension(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		try {
			DescribeStacksResult describeStackResult =
					cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName));
			Stack stack = describeStackResult.getStacks().get(0);
			
			if (stableStatuses.contains(stack.getStackStatus())) {
				Optional<ChangeSetSummary> summary = getLatestChangeSetSummary(cfn);
				String changeSetName = summary
					.orElseThrow(() -> new GradleException("ChangeSet for stack " + stackName + " was not found."))
					.getChangeSetName();
				ExecuteChangeSetRequest req = new ExecuteChangeSetRequest()
					.withStackName(stackName)
					.withChangeSetName(changeSetName);
				cfn.executeChangeSet(req);
				getLogger().info("ChangeSet is executed : {}, {}", stackName, changeSetName);
			} else {
				throw new GradleException("invalid status for update: " + stack.getStackStatus());
			}
		} catch (AmazonServiceException e) {
			if (e.getMessage().contains("does not exist")) {
				getLogger().warn("stack {} not found", stackName);
			} else if (e.getMessage().contains("No updates are to be performed.")) {
				getLogger().trace(e.getMessage());
			} else {
				throw e;
			}
		}
	}
	
	/**
	 *
	 * Return the latest ChangeSet Summary for the specified CloudFormation stack.
	 * @param cfn AmazonCloudFormation
	 * @return Optional
	 */
	private Optional<ChangeSetSummary> getLatestChangeSetSummary(AmazonCloudFormation cfn) {
		
		ListChangeSetsResult changeSetsResult =
				cfn.listChangeSets(new ListChangeSetsRequest().withStackName(getStackName()));
		List<ChangeSetSummary> changeSetSummaries = changeSetsResult.getSummaries();
		if (changeSetSummaries.isEmpty()) {
			return Optional.empty();
		}
		
		changeSetSummaries.sort(Comparator.comparing(ChangeSetSummary::getCreationTime).reversed());
		return Optional.of(changeSetSummaries.get(0));
	}
}
