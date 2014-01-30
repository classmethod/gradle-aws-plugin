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
import org.gradle.api.tasks.TaskAction

class AWSElasticBeanstalkCreateApplicationTask extends DefaultTask {
	
	{
		description 'Create/Migrate ElasticBeanstalk Application.'
		group = 'AWS'
	}
	
	def String appName
	
	def String appDesc = ''
	
	@TaskAction
	def createApplication() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		try {
			eb.createApplication(new CreateApplicationRequest()
				.withApplicationName(appName)
				.withDescription(appDesc))
			println "application $appName ($appDesc) created"
		} catch (AmazonClientException e) {
			if(e.message.endsWith('already exists.')) {
				eb.updateApplication(new UpdateApplicationRequest()
					.withApplicationName(appName)
					.withDescription(appDesc))
				println "application $appName ($appDesc) updated"
			} else {
				throw e
			}
		}
	}
}
