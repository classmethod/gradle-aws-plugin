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
	
	String appName
	
	String versionLabel
	
	String bucketName
	
	String key

	
	@TaskAction
	def createVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		try {
			eb.createApplicationVersion(new CreateApplicationVersionRequest()
				.withApplicationName(appName)
				.withVersionLabel(versionLabel)
				.withSourceBundle(new S3Location(bucketName, key)))
			logger.info "version $versionLabel @ $appName created"
		} catch(AmazonServiceException e) {
			if (e.getMessage().endsWith('already exists.') == false) {
				throw e
			}
			logger.warn "version $versionLabel @ $appName already exists."
		}
	}
}
