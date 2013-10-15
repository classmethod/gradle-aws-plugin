package jp.classmethod.aws.gradle.ec2

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.ec2.*
import com.amazonaws.services.ec2.model.*


class AmazonEC2StopInstanceTask extends DefaultTask {
	
	{
		description 'Stop EC2 instance.'
		group = 'AWS'
	}
	
	def List<String> instanceIds = []
	
	@TaskAction
	def createApplication() {
		if (instanceIds.isEmpty()) return
		def AmazonEC2 ec2 = project.aws.ec2
		try {
			ec2.stopInstances(new StopInstancesRequest(instanceIds))
			println "instance $instanceIds stop requested"
		} catch (AmazonClientException e) {
			throw e
		}
	}
}

class AmazonEC2StartInstanceTask extends DefaultTask {
	
	{
		description 'Start EC2 instance.'
		group = 'AWS'
	}
	
	def List<String> instanceIds = []
	
	@TaskAction
	def createApplication() {
		if (instanceIds.isEmpty()) return
		def AmazonEC2 ec2 = project.aws.ec2
		try {
			ec2.startInstances(new StartInstancesRequest(instanceIds))
			println "instance $instanceIds start requested"
		} catch (AmazonClientException e) {
			throw e
		}
	}
}

class AmazonEC2WaitInstanceStatusTask extends DefaultTask {
	
	{
		description 'Wait EC2 instance for specific status.'
		group = 'AWS'
	}
	
	def String instanceId
	
	def List<String> successStatuses = [
		'running',
		'stopped',
		'terminated'
	]

	def List<String> waitStatuses = [
		'pending',
		'shutting-down',
		'stopping'
	]
	
	def int loopTimeout = 900 // sec
	def int loopWait = 10 // sec
	
	def boolean found
	
	def String lastStatus
	
	@TaskAction
	def waitStackForStatus() {
		if (! instanceId) throw new GradleException("instanceId is not specified")
		def AmazonEC2 ec2 = project.aws.ec2

		def start = System.currentTimeMillis()
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException('Timeout')
			}
			try {
				def DescribeInstancesResult dir = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId))
				def Instance instance = dir.reservations[0].reservation[0]
				if (instance == null) {
					throw new GradleException("${instanceId} is not exists")
				}
				
				found = true
				lastStatus = instance.state.name
				if (successStatuses.contains(lastStatus)) {
					println "Status of ${instanceId} is now ${lastStatus}."
					break
				} else if (waitStatuses.contains(lastStatus)) {
					println "Status of stack ${instanceId} is ${lastStatus}..."
					Thread.sleep(loopWait * 1000)
				} else {
					// waitStatusesでもsuccessStatusesないステータスはfailとする
					throw new GradleException("Status of ${instanceId} is ${lastStatus}.  It seems to be failed.")
				}
			} catch (AmazonServiceException e) {
				if (found) {
					break
				} else {
					throw new GradleException("Fail to describe instance: ${instanceId}", e)
				}
			}
		}
	}
}

