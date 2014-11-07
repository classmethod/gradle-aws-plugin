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

import com.amazonaws.*
import com.amazonaws.services.cloudformation.*
import com.amazonaws.services.cloudformation.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class AmazonCloudFormationMigrateStackTask extends DefaultTask {
	
	{
		description 'Create/Migrate cfn stack.'
		group = 'AWS'
	}
	
	String stackName
	
	String cfnTemplateUrl
	
	List<Parameter> cfnStackParams = []
	
	boolean capabilityIam
	
	List<String> stableStatuses = [
		'CREATE_COMPLETE', 'ROLLBACK_COMPLETE', 'UPDATE_COMPLETE', 'UPDATE_ROLLBACK_COMPLETE'
	]
	
	
	@TaskAction
	def createOrUpdateStack() {
		// to enable conventionMappings feature
		String stackName = getStackName()
		String cfnTemplateUrl = getCfnTemplateUrl()
		boolean capabilityIam = isCapabilityIam()
		List<String> stableStatuses = getStableStatuses()
		
		if (! stackName) throw new GradleException("stackName is not specified")
		if (! cfnTemplateUrl) throw new GradleException("cfnTemplateUrl is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn
		
		try {
			def describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName))
			Stack stack = describeStackResult.stacks[0]
			if (stack == null) {
				logger.info "stack $stackName not found"
				createStack(cfn)
			} else if (stack.stackStatus == 'DELETE_COMPLETE') {
				logger.warn "deleted stack $stackName already exists"
				deleteStack(cfn)
				createStack(cfn)
			} else if (stableStatuses.contains(stack.stackStatus) == false) {
				throw new GradleException('invalid status for update: ' + stack.stackStatus)
			} else {
				updateStack(cfn)
			}
		} catch (AmazonServiceException e) {
			if (e.message.contains("does not exist")) {
				logger.warn "stack $stackName not found"
				createStack(cfn)
			} else if (e.message.contains("No updates are to be performed.")) {
				// ignore
			} else {
				throw e
			}
		}
	}
	
	private updateStack(AmazonCloudFormation cfn) {
		// to enable conventionMappings feature
		String stackName = getStackName()
		String cfnTemplateUrl = getCfnTemplateUrl()
		List<Parameter> cfnStackParams = getCfnStackParams()
		
		logger.info "update stack: $stackName"
		UpdateStackRequest req = new UpdateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams)
		if (isCapabilityIam()) {
			req.setCapabilities([Capability.CAPABILITY_IAM.toString()])
		}
		def updateStackResult = cfn.updateStack(req)
		logger.info "update requested: ${updateStackResult.stackId}"
	}
	
	private deleteStack(AmazonCloudFormation cfn) {
		// to enable conventionMappings feature
		String stackName = getStackName()

		logger.info "delete stack: $stackName"
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
		logger.info "delete requested: $stackName"
		Thread.sleep(3000)
	}
	
	private createStack(AmazonCloudFormation cfn) {
		// to enable conventionMappings feature
		String stackName = getStackName()
		String cfnTemplateUrl = getCfnTemplateUrl()
		List<Parameter> cfnStackParams = getCfnStackParams()

		logger.info "create stack: $stackName"
		
		CreateStackRequest req = new CreateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams)
		if (isCapabilityIam()) {
			req.setCapabilities([Capability.CAPABILITY_IAM.toString()])
		}
		def createStackResult = cfn.createStack(req)
		logger.info "create requested: ${createStackResult.stackId}"
	}
}

