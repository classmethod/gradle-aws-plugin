package jp.classmethod.aws.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.auth.*
import com.amazonaws.regions.*
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.cloudformation.*
import com.amazonaws.services.cloudformation.model.*
import com.amazonaws.services.ec2.*
import com.amazonaws.services.ec2.model.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*
import com.amazonaws.services.elasticloadbalancing.*
import com.amazonaws.services.route53.*
import com.amazonaws.services.route53.model.*
import com.amazonaws.internal.StaticCredentialsProvider

/**
 * A plugin which configures a AWS project.
 */
class AwsPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			project.extensions.create('aws', AwsPluginExtension)
		}
	}
}

class AwsPluginExtension {
	
	def String accessKeyId
	def String secretKey
	def Region region
	
	@Lazy AmazonEC2 ec2     = { configureRegion(new AmazonEC2Client(credentialsProvider)) }();
	@Lazy AmazonS3Client s3 = { configureRegion(new AmazonS3Client(credentialsProvider)) }();
	
	def void setRegion(String r) {
		region = RegionUtils.getRegion(r)
	}
	
	def void setRegion(Regions r) {
		region = RegionUtils.getRegion(r.name)
	}
	
	def AWSCredentialsProvider getCredentialsProvider() {
		(accessKeyId && secretKey) ?
			new StaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey)) :
			new DefaultAWSCredentialsProviderChain()
	}
	
	def <T extends AmazonWebServiceClient> T configureRegion(T client) {
		if (region != null) {
			client.setRegion(region)
		} else {
			println "region is null"
		}
		return client
	}
}