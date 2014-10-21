/*
 * Copyright 2013-2014 Classmethod, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.elasticbeanstalk

import groovy.lang.Closure;

import java.util.logging.Logger;

import jp.classmethod.aws.gradle.AwsPluginExtension
import jp.classmethod.aws.gradle.s3.AmazonS3ProgressiveFileUploadTask

import org.gradle.api.GradleException
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.util.Configurable;

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
			if (project.hasProperty('war')) {
				t.dependsOn(project.war)
			}
			t.onlyIf { ebExt.version.file != null || project.hasProperty('war') }
			t.doFirst {
				t.bucketName = ebExt.version.bucket
				t.key = ebExt.version.key
				if (project.hasProperty('war') && ebExt.version.file == null) {
					t.file = project.war.archivePath
				} else {
					t.file = ebExt.version.file
				}
			}
		}
		
		def awsEbCreateApplicationVersion = project.task('awsEbCreateApplicationVersion', type: AWSElasticBeanstalkCreateApplicationVersionTask) { AWSElasticBeanstalkCreateApplicationVersionTask t ->
			def dependency = [awsEbMigrateApplication]
			if (awsUploadWar) { dependency += awsUploadWar }
			t.dependsOn(dependency)
			
			t.doFirst {
				t.appName = ebExt.appName
				t.versionLabel = ebExt.version.getLabel()
				t.bucketName = ebExt.version.bucket
				t.key = ebExt.version.getKey()
			}
		}
		
		def awsEbMigrateConfigurationTemplates = project.task('awsEbMigrateConfigurationTemplates', type: AWSElasticBeanstalkCreateConfigurationTemplateTask) { AWSElasticBeanstalkCreateConfigurationTemplateTask t ->
			t.dependsOn awsEbMigrateApplication
			t.doFirst {
				t.appName = ebExt.appName
				t.configurationTemplates = ebExt.configurationTemplates
			}
		}
		
		def awsEbMigrateEnvironment = project.task('awsEbMigrateEnvironment', type: AWSElasticBeanstalkCreateEnvironmentTask) { AWSElasticBeanstalkCreateEnvironmentTask t ->
			t.dependsOn([awsEbMigrateConfigurationTemplates, awsEbCreateApplicationVersion])
			t.doFirst {
				t.appName = ebExt.appName
				t.envName = ebExt.environment.envName
				t.envDesc = ebExt.environment.envDesc
				t.templateName = ebExt.environment.templateName
				t.versionLabel = ebExt.environment.versionLabel
				t.tier = ebExt.tier ?: Tier.WebServer
			}
		}
		
		def awsEbTerminateEnvironment = project.task('awsEbTerminateEnvironment', type: AWSElasticBeanstalkTerminateEnvironmentTask) { AWSElasticBeanstalkTerminateEnvironmentTask t ->
			t.doFirst {
				t.appName = ebExt.appName
				t.envName = ebExt.environment.envName
			}
		}
		
		project.task('awsEbWaitEnvironmentReady', type: AWSElasticBeanstalkWaitEnvironmentStatusTask) { AWSElasticBeanstalkWaitEnvironmentStatusTask t ->
			t.mustRunAfter awsEbMigrateEnvironment
			t.doFirst {
				t.appName = ebExt.appName
				t.envName = ebExt.environment.envName
			}
		}
		
		def awsEbWaitEnvironmentTerminated = project.task('awsEbWaitEnvironmentTerminated', type: AWSElasticBeanstalkWaitEnvironmentStatusTask) { AWSElasticBeanstalkWaitEnvironmentStatusTask t ->
			t.mustRunAfter awsEbTerminateEnvironment
			t.doFirst {
				t.appName = ebExt.appName
				t.envName = ebExt.environment.envName
				t.successStatuses = [ 'Terminated' ]
				t.waitStatuses += 'Ready'
			}
		}
		
		def awsEbTerminateEnvironmentAndWaitTerminated = project.task('awsEbTerminateEnvironmentAndWaitTerminated') { Task t ->
			t.dependsOn([awsEbTerminateEnvironment, awsEbWaitEnvironmentTerminated])
		}
		
		project.task('awsEbCleanupApplicationVersions', type: AWSElasticBeanstalkCleanupApplicationVersionTask) { AWSElasticBeanstalkCleanupApplicationVersionTask t ->
			t.doFirst {
				t.appName = ebExt.appName
			}
		}
		
		project.task('awsEbDeleteApplication', type: AWSElasticBeanstalkDeleteApplicationTask) { AWSElasticBeanstalkDeleteApplicationTask t ->
			t.dependsOn awsEbTerminateEnvironmentAndWaitTerminated
			t.doFirst {
				t.appName = ebExt.appName
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
	
	EbAppVersionExtension version
	def version(Closure closure) {
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = version
		closure.call()
	}
	
	NamedDomainObjectContainer<EbConfigurationTemplateExtension> configurationTemplates
	def configurationTemplates(Closure cl) {
		configurationTemplates.configure(cl)
	}
	
	EbEnvironmentExtension environment
	def environment(Closure closure) {
		environment.configure(closure)
	}
	
	Tier tier = Tier.WebServer
	
	
	AwsBeanstalkPluginExtension(Project project) {
		this.project = project;
		this.version = project.container(EbAppVersionExtension)
		this.configurationTemplates = project.container(EbConfigurationTemplateExtension)
		this.environment = project.container(EbEnvironmentExtension)
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
		String elbName = env.endpointURL
		elbName = elbName.substring(0, elbName.indexOf('.'))
		elbName = elbName.substring(0, elbName.lastIndexOf('-'))
		return elbName
	}
}

class EbAppVersionExtension {
	
	def label
	String description = ''
	String bucket
	def key
	File file
	
	String getLabel() {
		if (label instanceof String) return label
		if (label instanceof Closure<String>) return label.call()
		return label?.toString()
	}
	
	String getKey() {
		if (key instanceof String) return key
		if (key instanceof Closure<String>) return key.call()
		return key?.toString()
	}
}

class EbEnvironmentExtension implements Configurable<Void> {
	
	String envName
	String envDesc = ''
	String templateName
	String versionLabel
	
	@Override
	public Void configure(Closure closure) {
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = this
		closure.call()
		return null
	}
}

class EbConfigurationTemplateExtension implements Named {
	
	String name
	String desc
	def private optionSettings
	String solutionStackName
    boolean recreate = false
	
	EbConfigurationTemplateExtension(String name) {
		this.name = name
	}
	
	String getOptionSettings() {
		if (optionSettings instanceof String) return optionSettings
		if (optionSettings instanceof Closure<String>) return optionSettings.call()
		return optionSettings?.toString()
	}
}