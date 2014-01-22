package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AWSElasticBeanstalkCreateEnvironmentTask extends DefaultTask {
	
	{
		description 'Create/Migrate ElasticBeanstalk Environment.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def String environmentName
	
	def String environmentDescription = ''
	
	def String cnamePrefix = java.util.UUID.randomUUID().toString()
	
	def String templateName
	
	def String versionLabel
	
	def Tier tier = Tier.WebServer
	
	@TaskAction
	def createEnvironment() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		try {
			DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(applicationName)
				.withEnvironmentNames(environmentName))
			
			if (der.environments == null || der.environments.isEmpty()) {
				throw new AmazonClientException("no env")
			}
			
			String environmentId = der.environments[0].environmentId
			
			eb.updateEnvironment(new UpdateEnvironmentRequest()
				.withEnvironmentId(environmentId)
				.withEnvironmentName(environmentName)
				.withDescription(environmentDescription)
				.withTemplateName(templateName)
				.withVersionLabel(versionLabel)
				.withTier(tier.toEnvironmentTier()))
			println "environment $environmentName @ $applicationName (${environmentId}) updated"
		} catch (AmazonClientException e) {
			CreateEnvironmentRequest req = new CreateEnvironmentRequest()
				.withApplicationName(applicationName)
				.withEnvironmentName(environmentName)
				.withDescription(environmentDescription)
				.withTemplateName(templateName)
				.withVersionLabel(versionLabel)
				.withTier(tier.toEnvironmentTier())
			if (tier == Tier.WebServer) {
				req.withCNAMEPrefix(cnamePrefix)
			}
			CreateEnvironmentResult result = eb.createEnvironment(req)
			println "environment $environmentName @ $applicationName (${result.environmentId}) created"
		}
	}
}

class AWSElasticBeanstalkTerminateEnvironmentTask extends DefaultTask {
	
	{
		description 'Terminate(Delete) ElasticBeanstalk Environment.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def String environmentName
	
	def String environmentId
	
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
