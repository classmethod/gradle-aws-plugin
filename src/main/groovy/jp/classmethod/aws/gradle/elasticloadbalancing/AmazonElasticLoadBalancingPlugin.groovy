package jp.classmethod.aws.gradle.elasticloadbalancing

import groovy.lang.Lazy;
import jp.classmethod.aws.gradle.AwsPluginExtension

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.regions.*
import com.amazonaws.services.elasticloadbalancing.*
import com.amazonaws.services.elasticloadbalancing.model.*


class AmazonElasticLoadBalancingPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			project.extensions.create(AmazonELBPluginExtension.NAME, AmazonELBPluginExtension, project)
		}
	}
}

class AmazonELBPluginExtension {
	
	public static final NAME = 'elb'
	
	Project project
	String accessKeyId
	String secretKey
	Region region
		
	@Lazy
	AmazonElasticLoadBalancing elb = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AmazonElasticLoadBalancingClient, region, accessKeyId, secretKey)
	}()
	
	AmazonELBPluginExtension(Project project) {
		this.project = project;
	}
}
