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
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class AWSElasticBeanstalkCleanupApplicationVersionTask extends DefaultTask {
	
	{
		description 'Cleanup unused SNAPSHOT ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	String appName
	
	boolean deleteSourceBundle = true

	
	@TaskAction
	def deleteVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(appName))
		List<String> usingVersions = der.environments.collect { EnvironmentDescription ed -> ed.versionLabel }
		
		DescribeApplicationVersionsResult davr = eb.describeApplicationVersions(new DescribeApplicationVersionsRequest()
			.withApplicationName(appName))
		List<String> versionLabelsToDelete = davr.applicationVersions.grep { ApplicationVersionDescription avd ->
			usingVersions.contains(avd.versionLabel) == false && avd.versionLabel.contains('-SNAPSHOT-')
		}.collect { ApplicationVersionDescription avd ->
			avd.versionLabel
		}
		
		versionLabelsToDelete.each { String versionLabel ->
			logger.info "version ${versionLabel} deleted"
			eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
				.withApplicationName(appName)
				.withVersionLabel(versionLabel)
				.withDeleteSourceBundle(deleteSourceBundle))

		}
	}
}
