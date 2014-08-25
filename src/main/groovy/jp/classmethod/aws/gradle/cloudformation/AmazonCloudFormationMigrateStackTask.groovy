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
		if (! getStackName()) throw new GradleException("stackName is not specified")
		if (! getCfnTemplateUrl()) throw new GradleException("cfnTemplateUrl is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn
		
		try {
			def describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName))
			Stack stack = describeStackResult.stacks[0]
			if (stack == null) {
				println "stack ${getStackName()} not found"
				createStack(cfn)
			} else if (stack.stackStatus == 'DELETE_COMPLETE') {
				println "deleted stack ${getStackName()} already exists"
				deleteStack(cfn)
				createStack(cfn)
			} else if (getStableStatuses().contains(stack.stackStatus) == false) {
				throw new GradleException('invalid status for update: ' + stack.stackStatus)
			} else {
				updateStack(cfn)
			}
		} catch (AmazonServiceException e) {
			if (e.message.contains("does not exist")) {
				println "stack ${getStackName()} not found"
				createStack(cfn)
			} else if (e.message.contains("No updates are to be performed.")) {
				// ignore
			} else {
				throw e
			}
		}
	}
	
	private updateStack(AmazonCloudFormation cfn) {
		println "update stack: ${getStackName()}"
		UpdateStackRequest req = new UpdateStackRequest()
				.withStackName(getStackName())
				.withTemplateURL(getCfnTemplateUrl())
				.withParameters(getCfnStackParams())
		if (isCapabilityIam()) {
			req.setCapabilities([Capability.CAPABILITY_IAM.toString()])
		}
		def updateStackResult = cfn.updateStack(req)
		println "update requested: ${updateStackResult.stackId}"
	}
	
	private deleteStack(AmazonCloudFormation cfn) {
		println "delete stack: ${getStackName()}"
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
		println "delete requested: ${getStackName()}"
		Thread.sleep(3000)
	}
	
	private createStack(AmazonCloudFormation cfn) {
		println "create stack: ${getStackName()}"
		
		CreateStackRequest req = new CreateStackRequest()
				.withStackName(getStackName())
				.withTemplateURL(getCfnTemplateUrl())
				.withParameters(getCfnStackParams())
		if (isCapabilityIam()) {
			req.setCapabilities([Capability.CAPABILITY_IAM.toString()])
		}
		def createStackResult = cfn.createStack(req)
		println "create requested: ${createStackResult.stackId}"
	}
}

