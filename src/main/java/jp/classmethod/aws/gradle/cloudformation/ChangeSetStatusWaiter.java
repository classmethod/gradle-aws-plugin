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

import java.util.List;
import java.util.Optional;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.ChangeSetSummary;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetResult;

/**
 * Waits for a status for a CloudFormation change set.
 */
public class ChangeSetStatusWaiter extends StatusWaiter {
	
	private final List<String> successStatuses;
	
	private final List<String> waitStatuses;
	
	
	public ChangeSetStatusWaiter(AmazonCloudFormation cfn, String stackName, Logger logger, List<String> stableStatuses,
			List<String> waitStatuses, int loopTimeout, int loopWait) throws InterruptedException {
		super(cfn, stackName, logger, loopTimeout, loopWait);
		this.successStatuses = stableStatuses;
		this.waitStatuses = waitStatuses;
	}
	
	@Override
	public GetStatusResult getStatus() {
		Optional<ChangeSetSummary> summary = new ChangeSetFetcher(cfn).getLatestChangeSetSummary(stackName);
		String changeSetName = summary
			.orElseThrow(() -> new GradleException("ChangeSet for stack " + stackName + " was not found."))
			.getChangeSetName();
		
		DescribeChangeSetRequest describeChangeSetRequest =
				new DescribeChangeSetRequest().withChangeSetName(changeSetName).withStackName(stackName);
		DescribeChangeSetResult describeChangeSetResult = cfn.describeChangeSet(describeChangeSetRequest);
		
		String status = describeChangeSetResult.getStatus();
		
		if ("FAILED".equals(status) && describeChangeSetResult.getChanges().isEmpty()) {
			logger.info("No updates to be performed.");
			return GetStatusResult.SUCCESS;
		}
		
		if (successStatuses.contains(status)) {
			return GetStatusResult.SUCCESS;
		} else if (waitStatuses.contains(status)) {
			return GetStatusResult.WAITING;
		} else {
			return GetStatusResult.FAILURE;
		}
	}
	
	@Override
	public String describeSubject() {
		return "change set for stack " + stackName;
	}
	
}
