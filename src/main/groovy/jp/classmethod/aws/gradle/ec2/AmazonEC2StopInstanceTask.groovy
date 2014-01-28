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
	
	List<String> instanceIds = []
	
	@TaskAction
	def createApplication() {
		if (instanceIds.isEmpty()) return
		
		AmazonEC2PluginExtension ext = project.extensions.getByType(AmazonEC2PluginExtension)
		AmazonEC2 ec2 = ext.ec2
		
		ec2.stopInstances(new StopInstancesRequest(instanceIds))
		println "instance $instanceIds stop requested"
	}
}
