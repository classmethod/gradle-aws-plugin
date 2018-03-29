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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonCloudFormationWaitStackStatusTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String stackName;
	
	@Getter
	@Setter
	private List<String> successStatuses = Arrays.asList(
			"CREATE_COMPLETE",
			"UPDATE_COMPLETE",
			"DELETE_COMPLETE");
	
	@Getter
	@Setter
	private List<String> waitStatuses = Arrays.asList(
			"CREATE_IN_PROGRESS",
			"ROLLBACK_IN_PROGRESS",
			"DELETE_IN_PROGRESS",
			"UPDATE_IN_PROGRESS",
			"UPDATE_COMPLETE_CLEANUP_IN_PROGRESS",
			"UPDATE_ROLLBACK_IN_PROGRESS",
			"UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS");
	
	@Getter
	@Setter
	private int loopTimeout = 900; // sec
	
	@Getter
	@Setter
	private int loopWait = 10; // sec
	
	@Getter
	@Setter
	private boolean found;
	
	@Getter
	@Setter
	private String lastStatus;
	
	@Getter
	@Setter
	private List<String> printedEvents;
	
	
	public AmazonCloudFormationWaitStackStatusTask() {
		super("AWS", "Wait cfn stack for specific status.");
	}
	
	@TaskAction
	public void waitStackForStatus() throws InterruptedException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		List<String> successStatuses = getSuccessStatuses();
		List<String> waitStatuses = getWaitStatuses();
		int loopTimeout = getLoopTimeout();
		int loopWait = getLoopWait();
		
		if (stackName == null) {
			throw new GradleException("stackName is not specified");
		}
		
		AmazonCloudFormationPluginExtension ext = getPluginExtension(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		long start = System.currentTimeMillis();
		printedEvents = new LinkedList<String>();
		
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException("Timeout");
			}
			try {
				// Get stack info
				DescribeStacksRequest describeStackRequest = new DescribeStacksRequest().withStackName(stackName);
				DescribeStacksResult describeStackResult = cfn.describeStacks(describeStackRequest);
				Stack stack = describeStackResult.getStacks().get(0);
				if (stack == null) {
					throw new GradleException("stack " + stackName + " is not exists");
				}
				found = true;
				lastStatus = stack.getStackStatus();
				
				// Get stack events info
				DescribeStackEventsRequest request = new DescribeStackEventsRequest().withStackName(stackName);
				DescribeStackEventsResult result = cfn.describeStackEvents(request);
				List<StackEvent> stackEvents = new LinkedList<StackEvent>(result.getStackEvents());
				Collections.reverse(stackEvents);
				
				// Always output new events; might be the last time you can
				printEvents(stackEvents);
				
				// If completed successfully, output status and outputs of stack, then break out of while loop
				if (successStatuses.contains(lastStatus)) {
					getLogger().info("Status of stack {} is now {}.", stackName, lastStatus);
					printOutputs(stack);
					break;
					
					// Else if still going, sleep some then loop again
				} else if (waitStatuses.contains(lastStatus)) {
					getLogger().info("Status of stack {} is {}...", stackName, lastStatus);
					Thread.sleep(loopWait * 1000);
					
					// Else, it must have failed, so get out of while loop
				} else {
					throw new GradleException(
							"Status of stack " + stackName + " is " + lastStatus + ".  It seems to be failed.");
				}
			} catch (AmazonServiceException e) {
				if (found) {
					break;
				} else {
					throw new GradleException("Fail to describe stack: " + stackName, e);
				}
			}
		}
	}
	
	private void printEvents(List<StackEvent> stackEvents) {
		if (printedEvents.isEmpty()) {
			getLogger().info("==== Events ====");
		}
		stackEvents.stream()
			.forEach(o -> {
				String eventId = o.getEventId();
				// If we haven't printed the event, then print it and add to list of printed events so we won't print again
				if (!printedEvents.contains(eventId)) {
					getLogger().info("{} {} {}: {} {}",
							o.getTimestamp(),
							o.getResourceStatus(),
							o.getResourceType(),
							o.getLogicalResourceId(),
							(o.getResourceStatusReason() == null) ? "" : o.getResourceStatusReason());
					printedEvents.add(eventId);
				}
			});
	}
	
	private void printOutputs(Stack stack) {
		getLogger().info("==== Outputs ====");
		stack.getOutputs().stream()
			.forEach(o -> getLogger().info("{} ({}) = {}", o.getOutputKey(), o.getDescription(), o.getOutputValue()));
	}
}
