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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.StackEvent;

/**
 * Waits for a particular status for a CloudFormation-related AWS resource. Subclasses must implement the behaviour
 * to get the status of the desired resource.
 */
public abstract class StatusWaiter {
	
	/**
	 * Gets the latest status of the resource we're interested in.
	 * @return the result of the status query.
	 */
	public abstract GetStatusResult getStatus();
	
	/**
	 * Describes the subject of the wait, for logging purposes.
	 * @return a String describing the AWS resource being waited on.
	 */
	public abstract String describeSubject();
	
	
	public enum GetStatusResult {
		SUCCESS, FAILURE, WAITING
	}
	
	
	protected final AmazonCloudFormation cfn;
	
	protected final String stackName;
	
	protected final Logger logger;
	
	private final int loopTimeout;
	
	private final int loopWait;
	
	
	protected StatusWaiter(AmazonCloudFormation cfn, String stackName, Logger logger, int loopTimeout,
			int loopWait) {
		this.cfn = cfn;
		this.stackName = stackName;
		this.logger = logger;
		this.loopTimeout = loopTimeout;
		this.loopWait = loopWait;
	}
	
	public void waitForSuccessStatus() throws InterruptedException {
		long start = System.currentTimeMillis();
		boolean found = false;
		GetStatusResult lastStatus;
		List<String> printedEvents = new ArrayList<>();
		
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException("Timeout");
			}
			try {
				lastStatus = getStatus();
				
				found = true;
				
				// Get stack events info
				List<StackEvent> stackEvents = getStackEvents(stackName);
				
				// Always output new events; might be the last time you can
				printEvents(stackEvents, printedEvents);
				
				// If completed successfully, output status then break out of while loop
				if (lastStatus == GetStatusResult.SUCCESS) {
					logger.info("Status of {} is now {}.", describeSubject(), lastStatus);
					break;
					
					// Else if still going, sleep some then loop again
				} else if (lastStatus == GetStatusResult.WAITING) {
					logger.info("Status of {} is {}...", describeSubject(), lastStatus);
					Thread.sleep(loopWait * 1000);
				} else {
					throw new GradleException(
							"Status of " + describeSubject() + " is " + lastStatus + ".  It seems to be failed.");
				}
			} catch (AmazonServiceException e) {
				if (found) {
					break;
				} else {
					throw new GradleException(String.format(Locale.ENGLISH, "Failed to get status for %s: ",
							describeSubject()), e);
				}
			}
		}
	}
	
	private List<StackEvent> getStackEvents(String stackName) {
		DescribeStackEventsRequest request = new DescribeStackEventsRequest().withStackName(stackName);
		DescribeStackEventsResult result = cfn.describeStackEvents(request);
		List<StackEvent> stackEvents = new LinkedList<>(result.getStackEvents());
		Collections.reverse(stackEvents);
		return stackEvents;
	}
	
	private void printEvents(List<StackEvent> stackEvents, List<String> printedEvents) {
		if (printedEvents.isEmpty()) {
			logger.info("==== Events ====");
		}
		stackEvents.stream()
			.forEach(o -> {
				String eventId = o.getEventId();
				// If we haven't printed the event, then print it and add to list of printed events so we won't print again
				if (!printedEvents.contains(eventId)) {
					logger.info("{} {} {}: {} {}",
							o.getTimestamp(),
							o.getResourceStatus(),
							o.getResourceType(),
							o.getLogicalResourceId(),
							(o.getResourceStatusReason() == null) ? "" : o.getResourceStatusReason());
					printedEvents.add(eventId);
				}
			});
	}
}
