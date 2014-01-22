package jp.classmethod.aws.gradle.s3

import com.amazonaws.AmazonClientException
import com.amazonaws.services.s3.AmazonS3Client
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction


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
		
		AmazonS3Client s3 = project.aws.s3
		if (ifExists == false || exists()) {
			s3.deleteBucket(bucketName)
			println "bucket $bucketName deleted"
		}
	}
	
	def boolean exists() {
		def AmazonS3Client s3 = project.aws.s3
		try {
			s3.getBucketLocation(bucketName)
			return true
		} catch (AmazonClientException e) {
			return false
		}
	}
}
