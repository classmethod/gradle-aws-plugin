package jp.classmethod.aws.gradle.cloudformation

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.DeleteStackRequest
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction;


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
