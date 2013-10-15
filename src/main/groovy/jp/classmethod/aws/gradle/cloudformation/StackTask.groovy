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
	
	def stackName
	
	def cfnTemplateUrl
	
	def cfnStackParams = []
	
	def List<String> stableStatuses = [
		'CREATE_COMPLETE', 'ROLLBACK_COMPLETE', 'UPDATE_COMPLETE', 'UPDATE_ROLLBACK_COMPLETE'
	]
	
	
	@TaskAction
	def createOrUpdateStack() {
		if (! stackName) throw new GradleException("stackName is not specified")
		if (! cfnTemplateUrl) throw new GradleException("cfnTemplateUrl is not specified")
		
		def AmazonCloudFormation cfn = project.aws.cfn
		
		try {
			def describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName))
			def Stack stack = describeStackResult.stacks[0]
			if (stack == null) {
				println "not exists"
				throw new AmazonServiceException("not exists")
			} else if (stack.stackStatus == 'DELETE_COMPLETE') {
				println "already exists"
				throw new AmazonServiceException("already exists")
			} else if (stableStatuses.contains(stack.stackStatus) == false) {
				throw new org.gradle.api.GradleException('invalid status for update: ' + stack.stackStatus)
			}
			println 'update stack: ' + stackName
			def updateStackResult = cfn.updateStack(new UpdateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams))
			println 'update requested: ' + updateStackResult.stackId
		} catch (AmazonServiceException e) {
			if (e.message == 'No updates are to be performed.') {
				// テンプレートに変化がなかった場合
				println 'No updates are to be performed.'
			} else {
				if (e.message.endsWith('already exists')) {
					println 'delete stack: ' + stackName
					cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
					println 'delete requested: ' + stackName
					Thread.sleep(3000)
				}
				
				println 'create stack: ' + stackName
				def createStackResult = cfn.createStack(new CreateStackRequest()
					.withStackName(stackName)
					.withTemplateURL(cfnTemplateUrl)
					.withParameters(cfnStackParams))
				println 'create requested: ' + createStackResult.stackId
			}
		}
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
		def AmazonCloudFormation cfn = project.aws.cfn
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
		println "delete stack $stackName requested"
	}
}
