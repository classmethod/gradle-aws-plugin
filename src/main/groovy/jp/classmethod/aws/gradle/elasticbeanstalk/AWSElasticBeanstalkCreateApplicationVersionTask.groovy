package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AWSElasticBeanstalkCreateApplicationVersionTask extends DefaultTask {
	
	{
		description 'Create/Migrate ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	String applicationName
	
	String versionLabel
	
	String bucketName
	
	String key

	
	@TaskAction
	def createVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		eb.createApplicationVersion(new CreateApplicationVersionRequest()
			.withApplicationName(applicationName)
			.withVersionLabel(versionLabel)
			.withSourceBundle(new S3Location(bucketName, key)))
		println "version ${versionLabel} created"
	}
}
