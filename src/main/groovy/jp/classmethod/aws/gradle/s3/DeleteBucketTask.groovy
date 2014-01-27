package jp.classmethod.aws.gradle.s3

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.amazonaws.AmazonClientException
import com.amazonaws.services.s3.*

public class DeleteBucketTask extends DefaultTask {
	
	{
		description 'Create the Amazon S3 bucket.'
		group = 'AWS'
	}
	
	def String bucketName
	
	def boolean ifExists
	
	@TaskAction
	def deleteBucket() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		if (ifExists == false || exists()) {
			s3.deleteBucket(bucketName)
			println "bucket $bucketName deleted"
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
