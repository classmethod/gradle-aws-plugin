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

import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class AWSElasticBeanstalkCreateConfigurationTemplateTask extends DefaultTask {

	{
		description 'Create/Migrate ElasticBeanstalk Configuration Templates.'
		group = 'AWS'
	}

	String appName

	Collection<EbConfigurationTemplateExtension> configurationTemplates = []

	String defaultSolutionStackName = '64bit Amazon Linux 2013.09 running Tomcat 7 Java 7'

	@TaskAction
	def createTemplate() {
		// to enable conventionMappings feature
		String appName = getAppName()

		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		configurationTemplates.each { EbConfigurationTemplateExtension config ->
			String templateName = config.name
			String templateDesc = config.desc
			String solutionStackName = config.solutionStackName ?: getDefaultSolutionStackName()
			boolean deleteTemplateIfExists = config.recreate

			ConfigurationOptionSetting[] optionSettings = loadConfigurationOptions(config.optionSettings)
			def existingApps = eb.describeApplications(new DescribeApplicationsRequest()
					.withApplicationNames(appName)).getApplications()
			if (existingApps.isEmpty()) {
				throw new IllegalArgumentException("App with name '${appName} does not exist")
			}

			if (existingApps.first().getConfigurationTemplates().contains(templateName)) {
				if (deleteTemplateIfExists) {
					eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
							.withApplicationName(appName)
							.withTemplateName(templateName))
					logger.info "configuration template $templateName @ $appName deleted"
				}
				else {
					eb.updateConfigurationTemplate(new UpdateConfigurationTemplateRequest()
							.withApplicationName(appName)
							.withTemplateName(templateName)
							.withDescription(templateDesc)
							.withOptionSettings(optionSettings))
					logger.info "configuration template $templateName @ $appName updated"
					return
				}
			}

			eb.createConfigurationTemplate(new CreateConfigurationTemplateRequest()
					.withApplicationName(appName)
					.withTemplateName(templateName)
					.withDescription(templateDesc)
					.withSolutionStackName(solutionStackName)
					.withOptionSettings(optionSettings))
			logger.info "configuration template $templateName @ $appName created"
		}
	}

	ConfigurationOptionSetting[] loadConfigurationOptions(String json) {
		List<ConfigurationOptionSetting> options = []
		new groovy.json.JsonSlurper().parseText(json).each {
			options += new ConfigurationOptionSetting(it.Namespace, it.OptionName, it.Value)
		}
		return options
	}
}
