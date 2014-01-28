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
	
	String applicationName
	
	String templateDescription = ''
	
	Map<String, Closure<String>> configurationTemplates = [:]
	
	String solutionStackName = '64bit Amazon Linux running Tomcat 7'
	
	@TaskAction
	def createTemplate() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		configurationTemplates.each {
			def templateName = it.key
			def optionSettings = loadConfigurationOptions(it.value)
			
			try {
				eb.createConfigurationTemplate(new CreateConfigurationTemplateRequest()
					.withApplicationName(applicationName)
					.withTemplateName(templateName)
					.withDescription(templateDescription)
					.withSolutionStackName(solutionStackName)
					.withOptionSettings(optionSettings))
				println "configuration template $templateName @ $applicationName created"
			} catch (AmazonClientException e) {
				eb.updateConfigurationTemplate(new UpdateConfigurationTemplateRequest()
					.withApplicationName(applicationName)
					.withTemplateName(templateName)
					.withDescription(templateDescription)
					.withOptionSettings(optionSettings))
				// TODO withOptionsToRemove ?
				println "configuration template $templateName @ $applicationName updated"
			}
		}
	}
	
	def ConfigurationOptionSetting[] loadConfigurationOptions(Closure<String> jsonClosure) {
		def options = []
		new groovy.json.JsonSlurper().parseText(jsonClosure.call()).each {
			options += new ConfigurationOptionSetting(it.Namespace, it.OptionName, it.Value)
		}
		return options
	}
}
