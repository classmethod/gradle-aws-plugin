package jp.classmethod.aws.gradle.cloudformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.services.cloudformation.model.UpdateStackResult;


public class AmazonCloudFormationMigrateStackTask extends ConventionTask {
	
	@Getter @Setter
	private String stackName;
	
	@Getter @Setter
	private String cfnTemplateUrl;
	
	@Getter @Setter
	private List<Parameter> cfnStackParams = new ArrayList<>();
	
	@Getter @Setter
	private boolean capabilityIam;
	
	@Getter @Setter
	private List<String> stableStatuses = Arrays.asList(
		"CREATE_COMPLETE", "ROLLBACK_COMPLETE", "UPDATE_COMPLETE", "UPDATE_ROLLBACK_COMPLETE"
	);
	
	public AmazonCloudFormationMigrateStackTask() {
		setDescription("Create / Migrate cfn stack.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void createOrUpdateStack() throws InterruptedException {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		List<String> stableStatuses = getStableStatuses();
		
		if (stackName == null) throw new GradleException("stackName is not specified");
		if (cfnTemplateUrl == null) throw new GradleException("cfnTemplateUrl is not specified");
		
		AmazonCloudFormationPluginExtension ext = getProject().getExtensions().getByType(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		try {
			DescribeStacksResult describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(stackName));
			Stack stack = describeStackResult.getStacks().get(0);
			if (stack == null) {
				getLogger().info("stack {} not found", stackName);
				createStack(cfn);
			} else if (stack.getStackStatus().equals("DELETE_COMPLETE")) {
				getLogger().warn("deleted stack {} already exists", stackName);
				deleteStack(cfn);
				createStack(cfn);
			} else if (stableStatuses.contains(stack.getStackStatus()) == false) {
				throw new GradleException("invalid status for update: " + stack.getStackStatus());
			} else {
				updateStack(cfn);
			}
		} catch (AmazonServiceException e) {
			if (e.getMessage().contains("does not exist")) {
				getLogger().warn("stack {} not found", stackName);
				createStack(cfn);
			} else if (e.getMessage().contains("No updates are to be performed.")) {
				// ignore
			} else {
				throw e;
			}
		}
	}
	
	private void updateStack(AmazonCloudFormation cfn) {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		List<Parameter> cfnStackParams = getCfnStackParams();
		
		getLogger().info("update stack: {}", stackName);
		UpdateStackRequest req = new UpdateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams);
		if (isCapabilityIam()) {
			req.setCapabilities(Arrays.asList(Capability.CAPABILITY_IAM.toString()));
		}
		UpdateStackResult updateStackResult = cfn.updateStack(req);
		getLogger().info("update requested: {}", updateStackResult.getStackId());
	}
	
	private void deleteStack(AmazonCloudFormation cfn) throws InterruptedException {
		// to enable conventionMappings feature
		String stackName = getStackName();

		getLogger().info("delete stack: {}", stackName);
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName));
		getLogger().info("delete requested: {}", stackName);
		Thread.sleep(3000);
	}
	
	private void createStack(AmazonCloudFormation cfn) {
		// to enable conventionMappings feature
		String stackName = getStackName();
		String cfnTemplateUrl = getCfnTemplateUrl();
		List<Parameter> cfnStackParams = getCfnStackParams();

		getLogger().info("create stack: {}", stackName);
		
		CreateStackRequest req = new CreateStackRequest()
				.withStackName(stackName)
				.withTemplateURL(cfnTemplateUrl)
				.withParameters(cfnStackParams);
		if (isCapabilityIam()) {
			req.setCapabilities(Arrays.asList(Capability.CAPABILITY_IAM.toString()));
		}
		CreateStackResult createStackResult = cfn.createStack(req);
		getLogger().info("create requested: {}", createStackResult.getStackId());
	}
}
