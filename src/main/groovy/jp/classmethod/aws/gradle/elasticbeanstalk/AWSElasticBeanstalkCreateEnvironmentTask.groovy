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

class AWSElasticBeanstalkCreateEnvironmentTask extends DefaultTask {

	{
		description 'Create/Migrate ElasticBeanstalk Environment.'
		group = 'AWS'
	}

	String appName

	String envName

	String envDesc = ''

	String cnamePrefix = java.util.UUID.randomUUID().toString()

	String templateName

	String versionLabel

	Tier tier = Tier.WebServer

	@TaskAction
	def createEnvironment() {
		// to enable conventionMappings feature
		String appName = getAppName()
		String envName = getEnvName()
		String envDesc = getEnvDesc()
		String cnamePrefix = getCnamePrefix()
		String templateName = getTemplateName()
		String versionLabel = getVersionLabel()
		Tier tier = getTier()
		
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb

		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(appName)
				.withEnvironmentNames(envName)
				.withIncludeDeleted(false))

		if (!der.environments || der.environments.isEmpty()) {
			CreateEnvironmentRequest req = new CreateEnvironmentRequest()
					.withApplicationName(appName)
					.withEnvironmentName(envName)
					.withDescription(envDesc)
					.withTemplateName(templateName)
					.withVersionLabel(versionLabel)
					.withTier(tier.toEnvironmentTier())
			if (tier == Tier.WebServer) {
				req.withCNAMEPrefix(cnamePrefix)
			}
			CreateEnvironmentResult result = eb.createEnvironment(req)
			logger.info "environment $envName @ $appName (${result.environmentId}) created"
		} else {
			def environmentId = der.environments.first().environmentId

			eb.updateEnvironment(new UpdateEnvironmentRequest()
					.withEnvironmentId(environmentId)
					.withEnvironmentName(envName)
					.withDescription(envDesc)
					.withTemplateName(templateName)
					.withVersionLabel(versionLabel)
					.withTier(tier.toEnvironmentTier()))
			logger.info "environment $envName @ $appName (${environmentId}) updated"
		}
	}
}
