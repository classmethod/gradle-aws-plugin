package jp.classmethod.aws.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.auth.*
import com.amazonaws.regions.*
import com.amazonaws.internal.StaticCredentialsProvider

/**
 * A plugin which configures a AWS project.
 */
class AwsPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			project.extensions.create(AwsPluginExtension.NAME, AwsPluginExtension, project)
		}
	}
}

class AwsPluginExtension {
	
	public static final NAME = 'aws'
	
	Project project;
	
	String accessKeyId
	
	String secretKey
	
	Region region = Region.getRegion(Regions.US_EAST_1)
	
	
	AwsPluginExtension(Project project) {
		this.project = project;
	}

	def void setRegion(String r) {
		region = RegionUtils.getRegion(r)
	}
	
	def void setRegion(Regions r) {
		region = RegionUtils.getRegion(r.name)
	}
	
	def AWSCredentialsProvider newCredentialsProvider(String accessKeyId, String secretKey) {
		return new AWSCredentialsProviderChain(
			new SystemPropertiesCredentialsProvider(),
			new StaticCredentialsProvider((accessKeyId && secretKey) ?
				new BasicAWSCredentials(accessKeyId, secretKey) : null),
			new StaticCredentialsProvider((this.accessKeyId && this.secretKey) ?
				new BasicAWSCredentials(this.accessKeyId, this.secretKey) : null)
		)
	}
	
	def <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, Region region = null, String accessKeyId = null, String secretKey = null) {
		if (region == null) {
			if (this.region == null) throw new IllegalStateException('default region is null')
			region = this.region
		}
		
		return region.createClient(serviceClass, newCredentialsProvider(accessKeyId, secretKey), null)
	}
}
