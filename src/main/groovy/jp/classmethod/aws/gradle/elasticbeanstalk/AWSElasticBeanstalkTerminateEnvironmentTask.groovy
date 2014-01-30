package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class AWSElasticBeanstalkTerminateEnvironmentTask extends DefaultTask {
	
	{
		description 'Terminate(Delete) ElasticBeanstalk Environment.'
		group = 'AWS'
	}
	
	String appName
	
	String envName
	
	String envId
	
	@TaskAction
	def terminateEnvironment() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		if (envId == null) {
			DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(appName)
				.withEnvironmentNames(envName))
			
			if (der.environments == null || der.environments.isEmpty()) {
				println "environment $envName @ $appName not found"
				return
			}
			
			EnvironmentDescription ed = der.environments[0]
			envId = ed.environmentId
		}
		
		try {
			eb.terminateEnvironment(new TerminateEnvironmentRequest()
				.withEnvironmentId(envId)
				.withEnvironmentName(envName))
			println "environment $envName (${envId}) @ $appName termination requested"
		} catch (AmazonServiceException e) {
			if (e.message.contains('No Environment found') == false) {
				throw e
			}
			println "environment $envName (${envId}) @ $appName not found"
		}
	}
}
