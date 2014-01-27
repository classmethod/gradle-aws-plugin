package jp.classmethod.aws.gradle.elasticbeanstalk

import jp.classmethod.aws.gradle.AwsPluginExtension
import jp.classmethod.aws.gradle.s3.AmazonS3ProgressiveFileUploadTask

import org.gradle.api.GradleException
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import com.amazonaws.*
import com.amazonaws.auth.*
import com.amazonaws.regions.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*

/**
 * A plugin which configures a AWS Elastic Beanstalk project.
 */
class AwsBeanstalkPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			apply plugin: 'aws-s3'
			apply plugin: 'war'
			project.extensions.create(AwsBeanstalkPluginExtension.NAME, AwsBeanstalkPluginExtension, project)
			applyTasks(project)
		}
	}
	
	void applyTasks(final Project project) {
		AwsBeanstalkPluginExtension ebExt = project.extensions.getByType(AwsBeanstalkPluginExtension)
		
		def awsEbMigrateApplication = project.task('awsEbMigrateApplication', type: AWSElasticBeanstalkCreateApplicationTask) { AWSElasticBeanstalkCreateApplicationTask t ->
			t.doFirst {
				t.appName = ebExt.appName
				t.appDesc = ebExt.appDesc
			}
		}
		
		def awsUploadWar = project.task('awsUploadWar', type: AmazonS3ProgressiveFileUploadTask) { AmazonS3ProgressiveFileUploadTask t ->
			t.dependsOn(project.war)
			t.doFirst {
				String extension = project.war.archiveName.tokenize('.').last()
				String baseName  = project.war.baseName
				String timestamp = new Date().format("yyyyMMdd'_'HHmmss", TimeZone.default)

				t.bucketName = ebExt.appBucket
				t.key = "${ebExt.appKeyPrefix}/${baseName}-${project.war.version}-${timestamp}.${extension}"
				t.file = project.war.archivePath
			}
		}
		
		def awsEbCreateApplicationVersion = project.task('awsEbCreateApplicationVersion', type: AWSElasticBeanstalkCreateApplicationVersionTask) { AWSElasticBeanstalkCreateApplicationVersionTask t ->
			t.dependsOn([awsUploadWar, awsEbMigrateApplication])
			t.doFirst {
				String extension = project.war.archiveName.tokenize('.').last()
				String baseName  = project.war.baseName
				String timestamp = new Date().format("yyyyMMdd'_'HHmmss", TimeZone.default)

				t.applicationName = ebExt.appName
				t.versionLabel = "${baseName}-${project.war.version}-${timestamp}"
				t.bucketName = ebExt.appBucket
				t.key = awsUploadWar.key
			}
		}
		
		def awsEbMigrateConfigurationTemplates = project.task('awsEbMigrateConfigurationTemplates', type: AWSElasticBeanstalkCreateConfigurationTemplateTask) { AWSElasticBeanstalkCreateConfigurationTemplateTask t ->
			t.dependsOn awsEbMigrateApplication
			t.doFirst {
				t.applicationName = ebExt.appName
				t.configurationTemplates = ebExt.configurationTemplates
			}
		}

		def awsEbMigrateEnvironment = project.task('awsEbMigrateEnvironment', type: AWSElasticBeanstalkCreateEnvironmentTask) { AWSElasticBeanstalkCreateEnvironmentTask t ->
			t.dependsOn([awsEbMigrateConfigurationTemplates, awsEbCreateApplicationVersion])
			t.doFirst {
				String envKey = project.hasProperty('targetEbEnv') ? project.targetEbEnv : ebExt.defaultEnv
				String envName = "${ebExt.envPrefix}-${envKey}"
				println "envKey = ${envKey} / envName = ${envName}"
				
				def EbEnvironmentExtension env = ebExt.environments[envKey]
				if (ebExt.productionProtection && env.role == EnvironmentRole.PRODUCTION) {
					throw new GradleException('You can\'t migrate PRODUCTION environment')
				}
				
				t.applicationName = ebExt.appName
				t.environmentName = envName
				t.templateName = env.configurationTemplate
				t.versionLabel = awsEbCreateApplicationVersion.versionLabel
				
				if (ebExt.tier) {
					t.tier = ebExt.tier
				}
			}
		}
		
		def awsEbTerminateEnvironment = project.task('awsEbTerminateEnvironment', type: AWSElasticBeanstalkTerminateEnvironmentTask) { AWSElasticBeanstalkTerminateEnvironmentTask t ->
			t.doFirst {
				String envKey = project.hasProperty('targetEbEnv') ? project.targetEbEnv : ebExt.defaultEnv
				String envName = "${ebExt.envPrefix}-${envKey}"
				println "envKey = ${envKey} / envName = ${envName}"
				
				def EbEnvironmentExtension env = ebExt.environments[envKey]
				if (ebExt.productionProtection && env.role == EnvironmentRole.PRODUCTION) {
					throw new GradleException('You can\'t terminate PRODUCTION environment')
				}
				
				t.applicationName = ebExt.appName
				t.environmentName = envName
			}
		}
		
		project.task('awsEbWaitEnvironmentReady', type: AWSElasticBeanstalkWaitEnvironmentStatusTask) { AWSElasticBeanstalkWaitEnvironmentStatusTask t ->
			t.mustRunAfter awsEbMigrateEnvironment
			t.doFirst {
				def envName = project.hasProperty('targetEbEnv') ? project.targetEbEnv : ebExt.defaultEnv
				envName = "${ebExt.envPrefix}-${envName}"

				t.applicationName = ebExt.appName
				t.environmentName = envName
			}
		}
		
		def awsEbWaitEnvironmentTerminated = project.task('awsEbWaitEnvironmentTerminated', type: AWSElasticBeanstalkWaitEnvironmentStatusTask) { AWSElasticBeanstalkWaitEnvironmentStatusTask t ->
			t.mustRunAfter awsEbTerminateEnvironment
			t.doFirst {
				def envName = project.hasProperty('targetEbEnv') ? project.targetEbEnv : ebExt.defaultEnv
				envName = "${ebExt.envPrefix}-${envName}"
				
				t.applicationName = ebExt.appName
				t.environmentName = envName
				t.successStatuses = [ 'Terminated' ]
				t.waitStatuses += 'Ready'
			}
		}
		
		def awsEbTerminateEnvironmentAndWaitTerminated = project.task('awsEbTerminateEnvironmentAndWaitTerminated') { Task t ->
			t.dependsOn([awsEbTerminateEnvironment, awsEbWaitEnvironmentTerminated])
		}
		
		project.task('awsEbCleanupApplicationVersions', type: AWSElasticBeanstalkCleanupApplicationVersionTask) { AWSElasticBeanstalkCleanupApplicationVersionTask t ->
			t.doFirst {
				t.applicationName = ebExt.appName
			}
		}
		
		project.task('awsEbDeleteApplication', type: AWSElasticBeanstalkDeleteApplicationTask) { AWSElasticBeanstalkDeleteApplicationTask t ->
			t.dependsOn awsEbTerminateEnvironmentAndWaitTerminated
			t.doFirst {
				t.applicationName = ebExt.appName
			}
		}
	}
}

class AwsBeanstalkPluginExtension {
	
	public static final NAME = 'beanstalk'

	Project project
	String accessKeyId
	String secretKey
	Region region
		
	@Lazy
	AWSElasticBeanstalk eb = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AWSElasticBeanstalkClient, region, accessKeyId, secretKey)
	}()
	
	String appName
	String appDesc = ''
	String appBucket
	String appKeyPrefix
	String keyName
	Tier tier
	Map<String, Closure<String>> configurationTemplates = [:]
	NamedDomainObjectContainer<EbEnvironmentExtension> environments
	String defaultEnv
	String envPrefix
	boolean productionProtection = true
	
	
	AwsBeanstalkPluginExtension(Project project) {
		this.project = project;
		this.environments = project.container(EbEnvironmentExtension)
	}
	
	def environments(Closure closure) {
		environments.configure(closure)
	}
	
	def configurationTemplates(Map<String, Closure<String>> configurationTemplates) {
		this.configurationTemplates = configurationTemplates
	}
	
	String getEbEnvironmentCNAME(String environmentName) {
		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(appName)
			.withEnvironmentNames(environmentName))
		EnvironmentDescription env = der.environments[0]
		return env.CNAME
	}
	
	EnvironmentDescription[] getEnvironmentDescs(List<String> environmentNames = Collections.emptyList()) {
		DescribeEnvironmentsRequest req = new DescribeEnvironmentsRequest().withApplicationName(appName)
		if (environmentNames.isEmpty() == false) {
			req.setEnvironmentNames(environmentNames)
		}
		DescribeEnvironmentsResult der = eb.describeEnvironments(req)
		return der.environments
	}
	
	String getElbName(EnvironmentDescription env) {
		String tmp = env.endpointURL
		tmp = tmp.substring(0, tmp.indexOf('.'))
		tmp = tmp.substring(0, tmp.lastIndexOf('-'))
		return tmp
	}
}

class EbEnvironmentExtension implements Named {
	
	String name
	String configurationTemplate
	String description = ''
	EnvironmentRole role
	
	EbEnvironmentExtension(String name) {
		this.name = name
	}
}

enum EnvironmentRole { PRODUCTION, STAGING, DEVELOPMENT }
