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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkTerminateEnvironmentTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private String envName;
	
	@Getter
	@Setter
	private String envId;
	
	
	public AWSElasticBeanstalkTerminateEnvironmentTask() {
		super("AWS", "Terminate(Delete) ElasticBeanstalk Environment.");
	}
	
	@TaskAction
	public void terminateEnvironment() {
		// to enable conventionMappings feature
		String appName = getAppName();
		String envName = getEnvName();
		String envId = getEnvId();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		if (envId == null) {
			DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(appName)
				.withEnvironmentNames(envName));
			
			if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
				getLogger().warn("environment " + envName + " @ " + appName + " not found");
				return;
			}
			
			EnvironmentDescription ed = der.getEnvironments().get(0);
			envId = ed.getEnvironmentId();
		}
		
		try {
			eb.terminateEnvironment(new TerminateEnvironmentRequest()
				.withEnvironmentId(envId)
				.withEnvironmentName(envName));
			getLogger().info("environment " + envName + " (" + envId + ") @ " + appName + " termination requested");
		} catch (AmazonServiceException e) {
			if (e.getMessage().contains("No Environment found") == false) {
				throw e;
			}
			getLogger().warn("environment " + envName + " (" + envId + ") @ " + appName + " not found");
		}
	}
}
