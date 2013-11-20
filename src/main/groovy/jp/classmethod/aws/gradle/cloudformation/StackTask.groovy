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
	
	def String stackName
	
	def String cfnTemplateUrl
	
	def cfnStackParams = []
	
	def boolean capabilityIam
	
	def List<String> stableStatuses = [
		'CREATE_COMPLETE', 'ROLLBACK_COMPLETE', 'UPDATE_COMPLETE', 'UPDATE_ROLLBACK_COMPLETE'
	]
	
	
	@TaskAction
	def createOrUpdateStack() {
		if (! stackName) throw new GradleException("stackName is not specified")
		if (! cfnTemplateUrl) throw new GradleException("cfnTemplateUrl is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn
		
		try {
			def describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName))
			def Stack stack = describeStackResult.stacks[0]
			if (stack == null) {
				println "stack ${stackName} not found"
				createStack(cfn)
			} else if (stack.stackStatus == 'DELETE_COMPLETE') {
				println "deleted stack ${stackName} already exists"
				deleteStack(cfn)
				createStack(cfn)
			} else if (stableStatuses.contains(stack.stackStatus) == false) {
				throw new org.gradle.api.GradleException('invalid status for update: ' + stack.stackStatus)
			} else {
				updateStack(cfn)
			}
		} catch (AmazonServiceException e) {
			if (e.message.contains("does not exist")) {
				println "stack ${stackName} not found"
				createStack(cfn)
			} else if (e.message.contains("No updates are to be performed.")) {
				// ignore
			} else {
				throw e
			}
		}
	}
	
	private updateStack(AmazonCloudFormation cfn) {
		println "update stack: $stackName"
		UpdateStackRequest req = new UpdateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams)
		if (capabilityIam) {
			req.setCapabilities(Capability.CAPABILITY_IAM)
		}
		def updateStackResult = cfn.updateStack(req)
		println "update requested: ${updateStackResult.stackId}"
	}
	
	private deleteStack(AmazonCloudFormation cfn) {
		println "delete stack: $stackName"
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
		println "delete requested: $stackName"
		Thread.sleep(3000)
	}
	
	private createStack(AmazonCloudFormation cfn) {
		println "create stack: $stackName"
		
		CreateStackRequest req = new CreateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams)
		if (capabilityIam) {
			req.setCapabilities(Capability.CAPABILITY_IAM)
		}
		def createStackResult = cfn.createStack(req)
		println "create requested: ${createStackResult.stackId}"
	}
}

class AmazonCloudFormationDeleteStackTask extends DefaultTask {
	
	{
		description 'Delete cfn stack.'
		group = 'AWS'
	}
	
	def stackName
	
	@TaskAction
	def deleteStack() {
		if (! stackName) throw new GradleException("stackName is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn
		
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
		println "delete stack $stackName requested"
	}
}
