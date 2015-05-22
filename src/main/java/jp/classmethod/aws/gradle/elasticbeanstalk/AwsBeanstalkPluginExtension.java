package jp.classmethod.aws.gradle.elasticbeanstalk;

import groovy.lang.Closure;

import java.util.List;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;


public class AwsBeanstalkPluginExtension {
	
	public static final String NAME = "beanstalk";

	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
		
	@Getter(lazy = true)
	private final AWSElasticBeanstalk client = initClient();
	
	private AWSElasticBeanstalk initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(
				AWSElasticBeanstalkClient.class,
				this.region == null ? null : RegionUtils.getRegion(this.region),
				profileName);
	}

	
	@Getter @Setter
	private	String appName;
	
	@Getter @Setter
	private	String appDesc = "";
	
	@Getter
	private	EbAppVersionExtension version;
	public void version(Closure<?> closure) {
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(version);
		closure.call();
	}
	
	@Getter
	private	NamedDomainObjectContainer<EbConfigurationTemplateExtension> configurationTemplates;
	public void configurationTemplates(Closure<?> closure) {
		configurationTemplates.configure(closure);
	}
	
	@Getter
	private	EbEnvironmentExtension environment;
	public void environment(Closure<?> closure) {
		environment.configure(closure);
	}
	
	@Getter @Setter
	private Tier tier = Tier.WebServer;
	
	
	public AwsBeanstalkPluginExtension(Project project) {
		this.project = project;
		this.version = new EbAppVersionExtension();
		this.configurationTemplates = project.container(EbConfigurationTemplateExtension.class);
		this.environment = new EbEnvironmentExtension();
	}
	
	public String getEbEnvironmentCNAME(String environmentName) {
		DescribeEnvironmentsResult der = getClient().describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(appName)
			.withEnvironmentNames(environmentName));
		EnvironmentDescription env = der.getEnvironments().get(0);
		return env.getCNAME();
	}
	
	public List<EnvironmentDescription> getEnvironmentDescs(List<String> environmentNames) {
		DescribeEnvironmentsRequest req = new DescribeEnvironmentsRequest().withApplicationName(appName);
		if (environmentNames.isEmpty() == false) {
			req.setEnvironmentNames(environmentNames);
		}
		DescribeEnvironmentsResult der = getClient().describeEnvironments(req);
		return der.getEnvironments();
	}
	
	public String getElbName(EnvironmentDescription env) {
		String elbName = env.getEndpointURL();
		elbName = elbName.substring(0, elbName.indexOf("."));
		elbName = elbName.substring(0, elbName.lastIndexOf("-"));
		return elbName;
	}
}
