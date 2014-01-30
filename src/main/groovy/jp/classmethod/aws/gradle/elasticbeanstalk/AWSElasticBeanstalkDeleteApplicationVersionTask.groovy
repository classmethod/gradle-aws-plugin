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

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class AWSElasticBeanstalkDeleteApplicationVersionTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	String applicationName
	
	String versionLabel
	
	String bucketName
	
	boolean deleteSourceBundle = true

	
	@TaskAction
	def deleteVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
			.withApplicationName(applicationName)
			.withVersionLabel(versionLabel)
			.withDeleteSourceBundle(deleteSourceBundle))
		println "version ${versionLabel} deleted"
	}
}
