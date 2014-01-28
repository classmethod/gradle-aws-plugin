package jp.classmethod.aws.gradle.s3

import groovy.lang.Lazy;
import jp.classmethod.aws.gradle.AwsPluginExtension

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.regions.*
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*


class AmazonS3Plugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			project.extensions.create(AmazonS3PluginExtension.NAME, AmazonS3PluginExtension, project)
		}
	}
}

class AmazonS3PluginExtension {
	
	public static final NAME = 's3'
	
	Project project
	String accessKeyId
	String secretKey
	Region region
		
	@Lazy
	AmazonS3 s3 = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AmazonS3Client, region, accessKeyId, secretKey)
	}()
	
	AmazonS3PluginExtension(Project project) {
		this.project = project;
	}
}
