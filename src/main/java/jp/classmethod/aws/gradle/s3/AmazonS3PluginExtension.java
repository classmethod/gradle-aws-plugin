package jp.classmethod.aws.gradle.s3;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


public class AmazonS3PluginExtension {
	
	public static final String NAME = "s3";
			
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region = Regions.US_EAST_1.getName();
		
	@Getter(lazy = true)
	private final AmazonS3 s3 = initClient();

	public AmazonS3PluginExtension(Project project) {
		this.project = project;
	}

	private AmazonS3 initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(AmazonS3Client.class, RegionUtils.getRegion(this.region), profileName);
	}
}
