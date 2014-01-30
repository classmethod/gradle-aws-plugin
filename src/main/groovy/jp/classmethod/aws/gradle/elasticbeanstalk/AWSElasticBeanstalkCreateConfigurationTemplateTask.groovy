package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.*
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
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		configurationTemplates.each { EbConfigurationTemplateExtension config ->
			String templateName = config.name
			String templateDesc = config.desc
			String solutionStackName = config.solutionStackName ?: defaultSolutionStackName
			ConfigurationOptionSetting[] optionSettings = loadConfigurationOptions(config.optionSettings)
			
			try {
				eb.createConfigurationTemplate(new CreateConfigurationTemplateRequest()
					.withApplicationName(appName)
					.withTemplateName(templateName)
					.withDescription(templateDesc)
					.withSolutionStackName(solutionStackName)
					.withOptionSettings(optionSettings))
				println "configuration template $templateName @ $appName created"
			} catch (AmazonClientException e) {
				eb.updateConfigurationTemplate(new UpdateConfigurationTemplateRequest()
					.withApplicationName(appName)
					.withTemplateName(templateName)
					.withDescription(templateDesc)
					.withOptionSettings(optionSettings))
				// TODO withOptionsToRemove ?
				println "configuration template $templateName @ $appName updated"
			}
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
