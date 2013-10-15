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
		def AWSElasticBeanstalk eb = project.aws.eb
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

class AWSElasticBeanstalkDeleteApplicationTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Application.'
		group = 'AWS'
	}
	
	def String applicationName
	
	@TaskAction
	def deleteApplication() {
		def AWSElasticBeanstalk eb = project.aws.eb
		eb.deleteApplication(new DeleteApplicationRequest(applicationName))
		println "application $applicationName deleted"
	}
}

