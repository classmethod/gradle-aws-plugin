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
