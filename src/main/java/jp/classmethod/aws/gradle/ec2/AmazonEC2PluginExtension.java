package jp.classmethod.aws.gradle.ec2;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;


public class AmazonEC2PluginExtension {
	
	public static final String NAME = "ec2";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
		
	@Getter(lazy = true)
	private final AmazonEC2 client = initClient();
	
	public AmazonEC2PluginExtension(Project project) {
		this.project = project;
	}

	private AmazonEC2 initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(
				AmazonEC2Client.class,
				this.region == null ? null : RegionUtils.getRegion(this.region),
				profileName);
	}
}
