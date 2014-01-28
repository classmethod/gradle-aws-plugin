package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class AWSElasticBeanstalkTerminateEnvironmentTask extends DefaultTask {
	
	{
		description 'Terminate(Delete) ElasticBeanstalk Environment.'
		group = 'AWS'
	}
	
	String applicationName
	
	String environmentName
	
	String environmentId
	
	@TaskAction
	def terminateEnvironment() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		if (environmentId == null) {
			DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(applicationName)
				.withEnvironmentNames(environmentName))
			
			if (der.environments == null || der.environments.isEmpty()) {
				println "environment $environmentName @ $applicationName not found"
				return
			}
			
			environmentId = der.environments[0].environmentId
		}
		
		try {
			eb.terminateEnvironment(new TerminateEnvironmentRequest()
				.withEnvironmentId(environmentId)
				.withEnvironmentName(environmentName))
			println "environment $environmentName @ $applicationName (${environmentId}) termination requested"
		} catch (AmazonServiceException e) {
			if (e.message.contains('No Environment found') == false) {
				throw e
			}
			println "environment $environmentName @ $applicationName (${environmentId}) not found"
		}
	}
}
