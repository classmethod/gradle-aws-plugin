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
	
	def String applicationName
	
	def String versionLabel
	
	def String bucketName
	
	def String key

	
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

class AWSElasticBeanstalkDeleteApplicationVersionTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def String versionLabel
	
	def String bucketName
	
	def boolean deleteSourceBundle = true

	
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

class AWSElasticBeanstalkCleanupApplicationVersionTask extends DefaultTask {
	
	{
		description 'Cleanup unused SNAPSHOT ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def boolean deleteSourceBundle = true

	
	@TaskAction
	def deleteVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		def Set<String> usingVersions = []
		def DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(applicationName))
		for (EnvironmentDescription ed in der.environments) {
			usingVersions += ed.versionLabel
//			println "version ${ed.versionLabel} is using"
		}
		
		def List<String> versionLabelsToDelete = []
		def DescribeApplicationVersionsResult davr = eb.describeApplicationVersions(new DescribeApplicationVersionsRequest()
			.withApplicationName(applicationName))
		for (ApplicationVersionDescription avd in davr.applicationVersions) {
			if (usingVersions.contains(avd.versionLabel) == false
					&& avd.versionLabel.contains('-SNAPSHOT-')) {
				versionLabelsToDelete += avd.versionLabel
			}
		}
		
		for (String versionLabel in versionLabelsToDelete) {
			println "version ${versionLabel} deleted"
			eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
				.withApplicationName(applicationName)
				.withVersionLabel(versionLabel)
				.withDeleteSourceBundle(deleteSourceBundle))
		}
	}
}