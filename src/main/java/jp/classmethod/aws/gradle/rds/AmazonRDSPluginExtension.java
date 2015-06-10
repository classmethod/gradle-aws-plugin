package jp.classmethod.aws.gradle.rds;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;


public class AmazonRDSPluginExtension {
	
	public static final String NAME = "rds";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
		
	@Getter(lazy = true)
	private final AmazonRDS client = initClient();
	
	public AmazonRDSPluginExtension(Project project) {
		this.project = project;
	}

	private AmazonRDS initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AmazonRDSClient client = aws.createClient(AmazonRDSClient.class, profileName);
		client.setRegion(aws.getActiveRegion(region));
		return client;
	}
}
