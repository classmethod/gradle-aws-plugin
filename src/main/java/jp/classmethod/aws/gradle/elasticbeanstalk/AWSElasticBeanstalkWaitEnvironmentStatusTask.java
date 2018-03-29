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
package jp.classmethod.aws.gradle.elasticbeanstalk;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkWaitEnvironmentStatusTask extends BaseAwsTask { // NOPMD
	
	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private String envName;
	
	@Getter
	@Setter
	private List<String> successStatuses = Arrays.asList(
			"Ready",
			"Terminated");
	
	@Getter
	@Setter
	private List<String> waitStatuses = Arrays.asList(
			"Launching",
			"Updating",
			"Terminating");
	
	@Getter
	@Setter
	private int loopTimeout = 900; // sec
	
	@Getter
	@Setter
	private int loopWait = 10; // sec
	
	
	public AWSElasticBeanstalkWaitEnvironmentStatusTask() {
		super("AWS", "Wait ElasticBeanstalk environment for specific status.");
	}
	
	@TaskAction
	public void waitEnvironmentForStatus() { // NOPMD
		// to enable conventionMappings feature
		String appName = getAppName();
		String envName = getEnvName();
		int loopTimeout = getLoopTimeout();
		int loopWait = getLoopWait();
		
		if (appName == null) {
			throw new GradleException("applicationName is not specified");
		}
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		long start = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException("Timeout");
			}
			
			try {
				DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
					.withApplicationName(appName)
					.withEnvironmentNames(envName));
				
				if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
					getLogger().info("environment " + envName + " @ " + appName + " not found");
					return;
				}
				
				EnvironmentDescription ed = der.getEnvironments().get(0);
				
				if (successStatuses.contains(ed.getStatus())) {
					getLogger()
						.info("Status of environment " + envName + " @ " + appName + " is now " + ed.getStatus() + ".");
					break;
				} else if (waitStatuses.contains(ed.getStatus())) {
					getLogger()
						.info("Status of environment " + envName + " @ " + appName + " is " + ed.getStatus() + "...");
					try {
						Thread.sleep(loopWait * 1000);
					} catch (InterruptedException e) {
						throw new GradleException("interrupted", e);
					}
				} else {
					// fail if not contains in successStatus or waitStatus
					throw new GradleException("Status of environment " + envName + " @ " + appName + " is "
							+ ed.getStatus() + ".  It seems to be failed.");
				}
			} catch (AmazonServiceException e) {
				throw new GradleException(e.getMessage(), e);
			}
		}
	}
}
