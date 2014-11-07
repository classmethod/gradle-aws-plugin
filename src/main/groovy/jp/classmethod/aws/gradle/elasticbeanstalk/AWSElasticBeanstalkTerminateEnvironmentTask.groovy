/*
 * Copyright 2013-2014 Classmethod, Inc.
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
package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class AWSElasticBeanstalkTerminateEnvironmentTask extends DefaultTask {
	
	{
		description 'Terminate(Delete) ElasticBeanstalk Environment.'
		group = 'AWS'
	}
	
	String appName
	
	String envName
	
	String envId
	
	@TaskAction
	def terminateEnvironment() {
		// to enable conventionMappings feature
		String appName = getAppName()
		String envName = getEnvName()
		String envId = getEnvId()
				
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		if (envId == null) {
			DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(appName)
				.withEnvironmentNames(envName))
			
			if (der.environments == null || der.environments.isEmpty()) {
				logger.warn "environment $envName @ $appName not found"
				return
			}
			
			EnvironmentDescription ed = der.environments[0]
			envId = ed.environmentId
		}
		
		try {
			eb.terminateEnvironment(new TerminateEnvironmentRequest()
				.withEnvironmentId(envId)
				.withEnvironmentName(envName))
			logger.info "environment $envName (${envId}) @ $appName termination requested"
		} catch (AmazonServiceException e) {
			if (e.message.contains('No Environment found') == false) {
				throw e
			}
			logger.warn "environment $envName (${envId}) @ $appName not found"
		}
	}
}
