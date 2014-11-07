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

class AWSElasticBeanstalkCreateApplicationVersionTask extends DefaultTask {
	
	{
		description 'Create/Migrate ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	String appName
	
	String versionLabel
	
	String bucketName
	
	String key

	
	@TaskAction
	def createVersion() {
		// to enable conventionMappings feature
		String appName = getAppName()
		String versionLabel = getVersionLabel()
		String bucketName = getBucketName()
		String key = getKey()
		
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		try {
			eb.createApplicationVersion(new CreateApplicationVersionRequest()
				.withApplicationName(appName)
				.withVersionLabel(versionLabel)
				.withSourceBundle(new S3Location(getBucketName(), getKey())))
			logger.info "version $versionLabel @ $appName created"
		} catch (AmazonServiceException e) {
			if (e.getMessage().endsWith('already exists.') == false) {
				throw e
			}
			logger.warn "version $versionLabel @ $appName already exists."
		}
	}
}
