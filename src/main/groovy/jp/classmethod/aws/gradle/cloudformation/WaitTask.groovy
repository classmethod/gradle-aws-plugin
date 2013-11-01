package jp.classmethod.aws.gradle.cloudformation

import com.amazonaws.*
import com.amazonaws.services.cloudformation.*
import com.amazonaws.services.cloudformation.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class AmazonCloudFormationWaitStackStatusTask extends DefaultTask {
	
	{
		description 'Wait cfn stack for specific status.'
		group = 'AWS'
	}
	
	def String stackName
	
	def List<String> successStatuses = [
		'CREATE_COMPLETE',
		'UPDATE_COMPLETE',
		'ROLLBACK_COMPLETE',
		'UPDATE_ROLLBACK_COMPLETE',
		'DELETE_COMPLETE',
	]

	def List<String> waitStatuses = [
		'CREATE_IN_PROGRESS',
		'ROLLBACK_IN_PROGRESS',
		'DELETE_IN_PROGRESS',
		'UPDATE_IN_PROGRESS',
		'UPDATE_COMPLETE_CLEANUP_IN_PROGRESS',
		'UPDATE_ROLLBACK_IN_PROGRESS',
		'UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS'
	]
	
	def int loopTimeout = 900 // sec
	def int loopWait = 10 // sec
	
	def boolean found
	
	def String lastStatus
	
	@TaskAction
	def waitStackForStatus() {
		if (! stackName) throw new GradleException("stackName is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn

		def start = System.currentTimeMillis()
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException('Timeout')
			}
			try {
				def describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName))
				def Stack stack = describeStackResult.stacks[0]
				if (stack == null) {
					throw new GradleException("stack ${stackName} is not exists")
				}
				found = true
				lastStatus = stack.stackStatus
				if (successStatuses.contains(lastStatus)) {
					println "Status of stack ${stackName} is now ${lastStatus}."
					break
				} else if (waitStatuses.contains(lastStatus)) {
					println "Status of stack ${stackName} is ${lastStatus}..."
					Thread.sleep(loopWait * 1000)
				} else {
					// waitStatusesでもsuccessStatusesないステータスはfailとする
					throw new GradleException("Status of stack ${stackName} is ${lastStatus}.  It seems to be failed.")
				}
			} catch (AmazonServiceException e) {
				if (found) {
					break
				} else {
					throw new GradleException("Fail to describe stack: ${stackName}", e)
				}
			}
		}
	}
}
