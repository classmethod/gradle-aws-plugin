package jp.classmethod.aws.gradle.route53;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;


public class AmazonRoute53PluginExtension {
	
	public static final String NAME = "route53";
			
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
			
	@Getter(lazy = true)
	private final AmazonRoute53 client = initClient();

	public AmazonRoute53PluginExtension(Project project) {
		this.project = project;
	}

	private AmazonRoute53 initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(
				AmazonRoute53Client.class,
				this.region == null ? null : RegionUtils.getRegion(this.region),
				profileName);
	}
}
