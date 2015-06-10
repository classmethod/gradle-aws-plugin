package jp.classmethod.aws.gradle.identitymanagement;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;


public class AmazonIdentityManagementPluginExtension {
	
	public static final String NAME = "iam";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter(lazy = true)
	private final AmazonIdentityManagement client = initClient();

	public AmazonIdentityManagementPluginExtension(Project project) {
		this.project = project;
	}

	private AmazonIdentityManagement initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AmazonIdentityManagement client = aws.createClient(AmazonIdentityManagementClient.class, profileName);
		return client;
	}
}
