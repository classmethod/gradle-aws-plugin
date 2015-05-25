package jp.classmethod.aws.gradle.cloudformation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;


public class AmazonCloudFormationPluginExtension {
	
	public static final String NAME = "cloudFormation";
	
	@Getter
	private final Project project;
	
	@Getter @Setter
	private String profileName;
	
	@Getter @Setter
	private String region;
	
	@Getter @Setter
	private String stackName;
	
	@Getter @Setter
	private Map<?, ?> stackParams = new HashMap<>();
	
	@Getter @Setter
	private String templateURL;
	
	@Getter @Setter
	private File templateFile;
	
	@Getter @Setter
	private String templateBucket;
	
	@Getter @Setter
	private String templateKeyPrefix;
	
	@Getter @Setter
	private boolean capabilityIam;
	
	@Getter(lazy = true)
	private final AmazonCloudFormation client = initClient();

	public AmazonCloudFormationPluginExtension(Project project) {
		this.project = project;
	}

	private AmazonCloudFormation initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AmazonCloudFormationClient client = aws.createClient(AmazonCloudFormationClient.class, profileName);
		client.setRegion(aws.getActiveRegion(region));
		return client;
	}

	public Stack getStack(String stackName) {
		return getClient().describeStacks(new DescribeStacksRequest().withStackName(stackName)).getStacks().get(0);
	}
	
	public List<StackResource> getStackResources(String stackName) {
		return getClient().describeStackResources(new DescribeStackResourcesRequest()
			.withStackName(stackName)).getStackResources();
	}
	
	public String getParameter(List<Parameter> parameters, String key) {
		return parameters.stream().filter(it -> it.getParameterKey().equals(key)).findFirst().get().getParameterValue(); 
	}
}
