package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class AWSElasticBeanstalkDeleteConfigurationTemplateTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Configuration Templates.'
		group = 'AWS'
	}
	
	String applicationName
	
	String templateName
	
	@TaskAction
	def deleteTemplate() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
			.withApplicationName(applicationName)
			.withTemplateName(templateName))
		
		println "configuration template $templateName @ $applicationName deleted"
	}
}
