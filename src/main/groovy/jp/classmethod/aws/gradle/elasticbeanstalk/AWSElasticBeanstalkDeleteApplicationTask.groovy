package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class AWSElasticBeanstalkDeleteApplicationTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Application.'
		group = 'AWS'
	}
	
	String applicationName
	
	@TaskAction
	def deleteApplication() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		eb.deleteApplication(new DeleteApplicationRequest(applicationName))
		println "application $applicationName deleted"
	}
}
