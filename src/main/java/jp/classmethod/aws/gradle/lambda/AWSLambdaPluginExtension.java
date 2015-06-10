package jp.classmethod.aws.gradle.lambda;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;


public class AWSLambdaPluginExtension {
	
	public static final String NAME = "lambda";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
		
	@Getter(lazy = true)
	private final AWSLambda client = initClient();
	
	public AWSLambdaPluginExtension(Project project) {
		this.project = project;
	}

	private AWSLambda initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AWSLambda client = aws.createClient(AWSLambdaClient.class, profileName);
		client.setRegion(aws.getActiveRegion(region));
		return client;
	}
}
