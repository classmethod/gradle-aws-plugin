package jp.classmethod.aws.gradle.s3

import com.amazonaws.services.s3.AmazonS3Client
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction;


class AmazonS3FileDeleteTask extends DefaultTask {
	
	{
		description = 'Delete file from the Amazon S3 bucket.'
		group = 'AWS'
	}

	String bucketName
	
	String key
	
	@TaskAction
	def delete() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		
		AmazonS3Client s3 = project.aws.s3
		println "deleting... ${bucketName}/${key}"
		s3.deleteObject(bucketName, key)
	}
}
