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
package jp.classmethod.aws.gradle.cloudformation

import java.util.List;

import groovy.lang.Closure
import groovy.lang.Lazy
import jp.classmethod.aws.gradle.AwsPlugin
import jp.classmethod.aws.gradle.AwsPluginExtension
import jp.classmethod.aws.gradle.s3.*

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask

import com.amazonaws.*
import com.amazonaws.auth.*
import com.amazonaws.auth.policy.actions.*
import com.amazonaws.regions.*
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.cloudformation.*
import com.amazonaws.services.cloudformation.model.*

/**
 * A plugin which configures a AWS project.
 */
class AmazonCloudFormationPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			apply plugin: 'aws-s3'
			extensions.create(AwsCloudFormationPluginExtension.NAME, AwsCloudFormationPluginExtension, project)
			applyTasks(project)
		}
	}
	
	void applyTasks(final Project project) {
		AwsCloudFormationPluginExtension cfnExt = project.extensions.getByType(AwsCloudFormationPluginExtension)
		
		project.task('awsCfnUploadTemplate', type: AmazonS3FileUploadTask) {
			description = 'Upload cfn template file to the Amazon S3 bucket.'
			conventionMapping.file = { cfnExt.getTemplateFile() }
			conventionMapping.bucketName = { cfnExt.getTemplateBucket() }
			conventionMapping.key = {
				String extension = cfnExt.templateFile.name.tokenize('.').last()
				String filename  = cfnExt.templateFile.name.tokenize('/').last()
				String baseName  = filename.substring(0, filename.length() - extension.length() - 1)
				String timestamp = new Date().format("yyyyMMdd'_'HHmmss", TimeZone.default)
				return "${cfnExt.templateKeyPrefix}/${baseName}-${project.version}-${timestamp}.${extension}".toString()
			}

			doLast {
				cfnExt.templateURL = resourceUrl
			}
		}
		
		project.task('awsCfnMigrateStack', type: AmazonCloudFormationMigrateStackTask) {
			description 'Create/Migrate cfn stack'
			mustRunAfter project.awsCfnUploadTemplate
			conventionMapping.stackName = { cfnExt.getStackName() }
			conventionMapping.capabilityIam = { cfnExt.getCapabilityIam() }
			conventionMapping.cfnStackParams = {
				cfnExt.stackParams.collect {
					new Parameter().withParameterKey(it.key).withParameterValue((String) it.value)
				}
			}
			conventionMapping.cfnTemplateUrl = { cfnExt.getTemplateURL() }
		}
		
		project.task('awsCfnWaitStackReady', type: AmazonCloudFormationWaitStackStatusTask) {
			description 'Wait cfn stack for *_COMPLETE status.'
			mustRunAfter project.awsCfnMigrateStack
			conventionMapping.stackName = { cfnExt.getStackName() }
		}
		
		project.task('awsCfnWaitStackComplete', type: AmazonCloudFormationWaitStackStatusTask) {
			description 'Wait cfn stack for CREATE_COMPETE or UPDATE_COMPLETE status.'
			mustRunAfter project.awsCfnMigrateStack
			successStatuses = [ 'CREATE_COMPLETE', 'UPDATE_COMPLETE' ]
			conventionMapping.stackName = { cfnExt.getStackName() }
		}
		
		project.task('awsCfnMigrateStackAndWaitCompleted') {
			description = 'Create/Migrate cfn stack, and wait stack for CREATE_COMPETE or UPDATE_COMPLETE status.'
			dependsOn project.awsCfnMigrateStack, project.awsCfnWaitStackComplete
		}
		
		project.task('awsCfnDeleteStack', type: AmazonCloudFormationDeleteStackTask) {
			description 'Delete cfn stack'
			conventionMapping.stackName = { cfnExt.getStackName() }
		}
		
		project.task('awsCfnWaitStackDeleted', type: AmazonCloudFormationWaitStackStatusTask) {
			description 'Wait cfn stack for DELETE_COMPLETE status.'
			mustRunAfter project.awsCfnDeleteStack
			successStatuses = [ 'DELETE_COMPLETE' ]
			conventionMapping.stackName = { cfnExt.getStackName() }
		}
		
		project.task('awsCfnDeleteStackAndWaitCompleted') {
			description 'Delete cfn stack, and wait stack for DELETE_COMPLETE status.'
			dependsOn project.awsCfnDeleteStack, project.awsCfnWaitStackDeleted
		}
	}
}

class AwsCloudFormationPluginExtension {
	
	public static final NAME = 'cloudFormation'
	
	final Project project
	String accessKeyId
	String secretKey
	Region region
	
	@Lazy
	AmazonCloudFormation cfn = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AmazonCloudFormationClient, region, accessKeyId, secretKey)
	}()
	
	String stackName
	Map<String, String> stackParams = [:]
	String templateURL
	
	File templateFile
	String templateBucket
	String templateKeyPrefix
	
	boolean capabilityIam
	
	AwsCloudFormationPluginExtension(Project project) {
		this.project = project;
	}

	Stack getStack(String stackName) {
		return cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName)).stacks[0]
	}
	
	List<StackResource> getStackResources(String stackName) {
		return cfn.describeStackResources(new DescribeStackResourcesRequest().withStackName(stackName)).stackResources
	}
	
	String getParameter(List<Parameter> parameters, String key) {
		return parameters.find { it.parameterKey == key }.parameterValue
	}
}
