package jp.classmethod.aws.gradle.elasticloadbalancing;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;


public class AmazonElasticLoadBalancingPluginExtension {
	
	public static final String NAME = "elb";
	
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
		
	@Getter(lazy = true)
	private final AmazonElasticLoadBalancing client = initClient();

	public AmazonElasticLoadBalancingPluginExtension(Project project) {
		this.project = project;
	}

	private AmazonElasticLoadBalancing initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AmazonElasticLoadBalancingClient client = aws.createClient( AmazonElasticLoadBalancingClient.class, profileName);
		client.setRegion(aws.getActiveRegion(region));
		return client;
	}
	
}
