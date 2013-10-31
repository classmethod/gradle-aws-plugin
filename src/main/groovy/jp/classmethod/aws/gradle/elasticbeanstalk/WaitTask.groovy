package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class AWSElasticBeanstalkWaitEnvironmentStatusTask extends DefaultTask {
	
	{
		description 'Wait ElasticBeanstalk environment for specific status.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def String environmentName
	
	def List<String> successStatuses = [
		'Ready',
		'Terminated'
	]

	def List<String> waitStatuses = [
		'Launching',
		'Updating',
		'Terminating'
	]
	
	def int loopTimeout = 900 // sec
	def int loopWait = 10 // sec
	
	@TaskAction
	def waitEnvironmentForStatus() {
		if (! applicationName) throw new GradleException("applicationName is not specified")
		
		def start = System.currentTimeMillis()
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException('Timeout')
			}

			AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
			AWSElasticBeanstalk eb = ext.eb

			try {
				def DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
					.withApplicationName(applicationName)
					.withEnvironmentNames(environmentName))
				
				if(der.environments == null || der.environments.isEmpty()) {
					println "environment $environmentName @ $applicationName not found"
					return
				}
				
				EnvironmentDescription env = der.environments[0]
				
				if (successStatuses.contains(env.status)) {
					println "Status of environment ${environmentName} @ $applicationName is now ${env.status}."
					break
				} else if (waitStatuses.contains(env.status)) {
					println "Status of environment ${environmentName} @ $applicationName is ${env.status}..."
					Thread.sleep(loopWait * 1000)
				} else {
					// waitStatusesでもsuccessStatusesないステータスはfailとする
					throw new GradleException("Status of environment ${environmentName} @ $applicationName is ${env.status}.  It seems to be failed.")
				}
			} catch (AmazonServiceException e) {
				throw new GradleException(e)
			}
		}
	}
}