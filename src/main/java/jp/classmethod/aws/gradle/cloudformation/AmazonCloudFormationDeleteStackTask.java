package jp.classmethod.aws.gradle.cloudformation;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;


public class AmazonCloudFormationDeleteStackTask extends ConventionTask {
	
	@Getter @Setter
	private String stackName;
	
	public AmazonCloudFormationDeleteStackTask() {
		setDescription("Delete cfn stack.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void deleteStack() {
		// to enable conventionMappings feature
		String stackName = getStackName();

		if (stackName == null) throw new GradleException("stackName is not specified");
		
		AwsCloudFormationPluginExtension ext = getProject().getExtensions().getByType(AwsCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getCfn();
		
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName));
		getLogger().info("delete stack "+stackName+" requested");
	}
}
