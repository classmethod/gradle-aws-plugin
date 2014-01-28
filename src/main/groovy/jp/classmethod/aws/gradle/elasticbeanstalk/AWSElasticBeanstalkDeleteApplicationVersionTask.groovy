package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class AWSElasticBeanstalkDeleteApplicationVersionTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	String applicationName
	
	String versionLabel
	
	String bucketName
	
	boolean deleteSourceBundle = true

	
	@TaskAction
	def deleteVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
			.withApplicationName(applicationName)
			.withVersionLabel(versionLabel)
			.withDeleteSourceBundle(deleteSourceBundle))
		println "version ${versionLabel} deleted"
	}
}
