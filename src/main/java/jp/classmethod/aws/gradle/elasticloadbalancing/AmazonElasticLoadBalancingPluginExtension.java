package jp.classmethod.aws.gradle.elasticloadbalancing;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;


public class AmazonElasticLoadBalancingPluginExtension {
	
	public static final String NAME = "elb";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region = Regions.US_EAST_1.getName();
		
	@Getter(lazy = true)
	private final AmazonElasticLoadBalancing elb = initClient();

	public AmazonElasticLoadBalancingPluginExtension(Project project) {
		this.project = project;
	}

	private AmazonElasticLoadBalancing initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(
				AmazonElasticLoadBalancingClient.class,
				this.region == null ? null : RegionUtils.getRegion(this.region),
				profileName);
	}
	
}
