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
package jp.classmethod.aws.gradle.ec2;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonEC2WaitInstanceStatusTask extends BaseAwsTask { // NOPMD
	
	@Getter
	@Setter
	private String instanceId;
	
	@Getter
	@Setter
	private List<String> successStatuses = Arrays.asList(
			"running",
			"stopped",
			"terminated");
	
	@Getter
	@Setter
	private List<String> waitStatuses = Arrays.asList(
			"pending",
			"shutting-down",
			"stopping");
	
	@Getter
	@Setter
	private int loopTimeout = 900; // sec
	
	@Getter
	@Setter
	private int loopWait = 10; // sec
	
	@Getter
	private boolean found;
	
	@Getter
	private String lastStatus;
	
	@Getter
	private Instance awsInstance;
	
	
	public AmazonEC2WaitInstanceStatusTask() {
		super("AWS", "Wait EC2 instance for specific status.");
	}
	
	@TaskAction
	public void waitInstanceForStatus() { // NOPMD
		// to enable conventionMappings feature
		String instanceId = getInstanceId();
		List<String> successStatuses = getSuccessStatuses();
		List<String> waitStatuses = getWaitStatuses();
		int loopTimeout = getLoopTimeout();
		int loopWait = getLoopWait();
		
		if (instanceId == null) {
			throw new GradleException("instanceId is not specified");
		}
		
		AmazonEC2PluginExtension ext = getPluginExtension(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		long start = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException("Timeout");
			}
			try {
				DescribeInstancesResult dir = ec2.describeInstances(new DescribeInstancesRequest()
					.withInstanceIds(instanceId));
				Instance instance = dir.getReservations().get(0).getInstances().get(0);
				awsInstance = instance;
				if (instance == null) {
					throw new GradleException(instanceId + " is not exists");
				}
				
				found = true;
				lastStatus = instance.getState().getName();
				if (successStatuses.contains(lastStatus)) {
					getLogger().info("Status of instance {} is now {}.", instanceId, lastStatus);
					break;
				} else if (waitStatuses.contains(lastStatus)) {
					getLogger().info("Status of instance {} is {}...", instanceId, lastStatus);
					try {
						Thread.sleep(loopWait * 1000);
					} catch (InterruptedException e) {
						throw new GradleException("Sleep interrupted", e);
					}
				} else {
					// fail when current status is not waitStatuses or successStatuses
					throw new GradleException(
							"Status of " + instanceId + " is " + lastStatus + ".  It seems to be failed.");
				}
			} catch (AmazonServiceException e) {
				if (found) {
					break;
				} else {
					throw new GradleException("Fail to describe instance: " + instanceId, e);
				}
			}
		}
	}
}
