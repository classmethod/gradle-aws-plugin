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
	
	def String bucketName
	
	def boolean ifNotExists
	
	@TaskAction
	def createBucket() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		
		def AmazonS3Client s3 = project.aws.s3
		if (ifNotExists == false || exists() == false) {
			s3.createBucket(bucketName)
			println "bucket $bucketName created"
		}
	}
	
	def boolean exists() {
		def AmazonS3Client s3 = project.aws.s3
		try {
			s3.getBucketLocation(bucketName)
			true
		} catch (AmazonClientException e) {
			false
		}
	}
}

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
		
		def AmazonS3Client s3 = project.aws.s3
		if (ifExists == false || exists()) {
			s3.deleteBucket(bucketName)
			println "bucket $bucketName deleted"
		}
	}
	
	def boolean exists() {
		def AmazonS3Client s3 = project.aws.s3
		try {
			s3.getBucketLocation(bucketName)
			true
		} catch (AmazonClientException e) {
			false
		}
	}
}
