package jp.classmethod.aws.gradle.s3

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectSummary
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction;


class AmazonS3DeleteAllFilesTask extends DefaultTask {
	
	{
		description = 'Delete all files on S3 bucket.'
		group = 'AWS'
	}

	String bucketName
	
	String prefix = ''
	
	@TaskAction
	def delete() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		
		AmazonS3Client s3 = project.aws.s3
		String prefix = this.prefix.startsWith('/') ? this.prefix.substring(1) : this.prefix
		println "deleting... ${bucketName}/${prefix}"
		
		List<S3ObjectSummary> objectSummaries
		while ((objectSummaries = s3.listObjects(bucketName, prefix).objectSummaries).isEmpty() == false) {
			objectSummaries.each { S3ObjectSummary os ->
				println "deleting... s3://${bucketName}/${os.key}"
				s3.deleteObject(bucketName, os.key)
			}
		}
	}
}
