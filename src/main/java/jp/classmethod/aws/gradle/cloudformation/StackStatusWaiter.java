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

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;

/**
 * Waits for a status for a CloudFormation stack.
 */
public class StackStatusWaiter extends StatusWaiter {
	
	private final List<String> successStatuses;
	
	private final List<String> waitStatuses;
	
	
	public StackStatusWaiter(AmazonCloudFormation cfn, String stackName, Logger logger, List<String> successStatuses,
			List<String> waitStatuses, int loopTimeout, int loopWait) {
		super(cfn, stackName, logger, loopTimeout, loopWait);
		this.successStatuses = successStatuses;
		this.waitStatuses = waitStatuses;
	}
	
	@Override
	public GetStatusResult getStatus() {
		// Get stack info
		DescribeStacksRequest describeStackRequest = new DescribeStacksRequest().withStackName(stackName);
		DescribeStacksResult describeStackResult = cfn.describeStacks(describeStackRequest);
		Stack stack = describeStackResult.getStacks().get(0);
		if (stack == null) {
			throw new GradleException("stack " + stackName + " does not exist");
		}
		
		String status = stack.getStackStatus();
		
		if (successStatuses.contains(status)) {
			printOutputs(stack);
			return GetStatusResult.SUCCESS;
		} else if (waitStatuses.contains(status)) {
			return GetStatusResult.WAITING;
		} else {
			return GetStatusResult.FAILURE;
		}
		
	}
	
	@Override
	public String describeSubject() {
		return "stack " + stackName;
	}
	
	private void printOutputs(Stack stack) {
		logger.info("==== Outputs ====");
		stack.getOutputs().stream()
			.forEach(o -> logger.info("{} ({}) = {}", o.getOutputKey(), o.getDescription(), o.getOutputValue()));
	}
}
