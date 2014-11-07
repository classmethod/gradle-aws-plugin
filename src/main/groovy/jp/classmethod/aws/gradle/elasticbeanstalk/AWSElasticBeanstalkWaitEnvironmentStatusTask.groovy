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

import com.amazonaws.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class AWSElasticBeanstalkWaitEnvironmentStatusTask extends DefaultTask {
	
	{
		description 'Wait ElasticBeanstalk environment for specific status.'
		group = 'AWS'
	}
	
	String appName
	
	String envName
	
	List<String> successStatuses = [
		'Ready',
		'Terminated'
	]

	List<String> waitStatuses = [
		'Launching',
		'Updating',
		'Terminating'
	]
	
	int loopTimeout = 900 // sec
	int loopWait = 10 // sec
	
	@TaskAction
	def waitEnvironmentForStatus() {
		// to enable conventionMappings feature
		String appName = getAppName()
		String envName = getEnvName()
		int loopTimeout = getLoopTimeout()
		int loopWait = getLoopWait()

		if (! appName) throw new GradleException("applicationName is not specified")
		
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb

		def start = System.currentTimeMillis()
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException('Timeout')
			}

			try {
				DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
					.withApplicationName(appName)
					.withEnvironmentNames(envName))
				
				if (der.environments == null || der.environments.isEmpty()) {
					logger.info "environment $envName @ $appName not found"
					return
				}
				
				EnvironmentDescription ed = der.environments[0]
				
				if (successStatuses.contains(ed.status)) {
					logger.info "Status of environment $envName @ $appName is now ${ed.status}."
					break
				} else if (waitStatuses.contains(ed.status)) {
					logger.info "Status of environment $envName @ $appName is ${ed.status}..."
					Thread.sleep(loopWait * 1000)
				} else {
					// waitStatusesでもsuccessStatusesないステータスはfailとする
					throw new GradleException("Status of environment $envName @ $appName is ${ed.status}.  It seems to be failed.")
				}
			} catch (AmazonServiceException e) {
				throw new GradleException(e)
			}
		}
	}
}