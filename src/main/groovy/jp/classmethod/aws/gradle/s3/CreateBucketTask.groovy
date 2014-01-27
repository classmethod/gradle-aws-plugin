package jp.classmethod.aws.gradle.s3

import com.amazonaws.*;
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction


public class CreateBucketTask extends DefaultTask {
	
	{
		description 'Create the Amazon S3 bucket.'
		group = 'AWS'
	}
	
	String bucketName
	
	boolean ifNotExists
	
	@TaskAction
	def createBucket() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		if (ifNotExists == false || exists() == false) {
			s3.createBucket(bucketName)
			println "bucket $bucketName created"
		}
	}
	
	boolean exists() {
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		try {
			s3.getBucketLocation(bucketName)
			return true
		} catch (AmazonClientException e) {
			return false
		}
	}
}
