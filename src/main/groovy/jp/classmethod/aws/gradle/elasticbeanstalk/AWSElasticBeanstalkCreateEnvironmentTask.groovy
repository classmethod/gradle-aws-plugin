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
	
	String appName
	
	String envName
	
	String envDesc = ''
	
	String cnamePrefix = java.util.UUID.randomUUID().toString()
	
	String templateName
	
	String versionLabel
	
	Tier tier = Tier.WebServer
	
	@TaskAction
	def createEnvironment() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		try {
			DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(appName)
				.withEnvironmentNames(envName))
			
			if (der.environments == null || der.environments.isEmpty()) {
				throw new AmazonClientException("no env")
			}
			
			EnvironmentDescription ed = der.environments[0]
			String environmentId = ed.environmentId
			
			eb.updateEnvironment(new UpdateEnvironmentRequest()
				.withEnvironmentId(environmentId)
				.withEnvironmentName(envName)
				.withDescription(envDesc)
				.withTemplateName(templateName)
				.withVersionLabel(versionLabel)
				.withTier(tier.toEnvironmentTier()))
			println "environment $envName @ $appName (${environmentId}) updated"
		} catch (AmazonClientException e) {
			CreateEnvironmentRequest req = new CreateEnvironmentRequest()
				.withApplicationName(appName)
				.withEnvironmentName(envName)
				.withDescription(envDesc)
				.withTemplateName(templateName)
				.withVersionLabel(versionLabel)
				.withTier(tier.toEnvironmentTier())
			if (tier == Tier.WebServer) {
				req.withCNAMEPrefix(cnamePrefix)
			}
			CreateEnvironmentResult result = eb.createEnvironment(req)
			println "environment $envName @ $appName (${result.environmentId}) created"
		}
	}
}
