package jp.classmethod.aws.gradle.ec2

import com.amazonaws.AmazonClientException
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.StartInstancesRequest
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class AmazonEC2StartInstanceTask extends DefaultTask {
	
	{
		description 'Start EC2 instance.'
		group = 'AWS'
	}
	
	List<String> instanceIds = []
	
	@TaskAction
	def createApplication() {
		if (instanceIds.isEmpty()) return
		
		AmazonEC2 ec2 = project.aws.ec2
		ec2.startInstances(new StartInstancesRequest(instanceIds))
		println "instance $instanceIds start requested"
	}
}
