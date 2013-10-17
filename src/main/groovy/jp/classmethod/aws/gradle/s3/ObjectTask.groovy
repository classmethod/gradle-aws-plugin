package jp.classmethod.aws.gradle.s3

import java.util.List;

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class AmazonS3FileUploadTask extends DefaultTask {
	
	{
		description = 'Upload file to the Amazon S3 bucket.'
		group = 'AWS'
	}

	def String bucketName
	
	def String key
	
	def File file
	
	def boolean overwrite = true
	
	// == after did work
	
	def String resourceUrl
	
	@TaskAction
	def upload() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		if (! file) throw new GradleException("file is not specified")
		
		def AmazonS3Client s3 = project.aws.s3
		println "uploading... ${bucketName}/${key}"
		resourceUrl = s3.getResourceUrl(bucketName, key)
		if (overwrite || exists() == false) {
			s3.putObject(bucketName, key, file)
			println "upload completed: $resourceUrl"
		} else {
			println "${bucketName}/${key} is already exists -- skipped"
		}
	}
	
	def boolean exists() {
		def AmazonS3Client s3 = project.aws.s3
		try {
			ObjectMetadata objectMetadata = s3.getObjectMetadata(bucketName, key);
			return true
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() == 404) {
				return false
			}
			throw e;    // rethrow all S3 exceptions other than 404
		}
	}
}

class AmazonS3ProgressiveFileUploadTask extends DefaultTask {
	
	{
		description 'Upload war file to the Amazon S3 bucket.'
		group = 'AWS'
	}
	
	def String bucketName
	
	def String key
	
	def File file
	
	// == after did work
	
	def String resourceUrl
	
	@TaskAction
	def upload() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		if (! file) throw new GradleException("file is not specified")
		
		def AmazonS3Client s3 = project.aws.s3
		def s3mgr = new TransferManager(s3)
		println "uploading... ${bucketName}/${key}"
		def Upload upload = s3mgr.upload(bucketName, key, file)
		upload.addProgressListener(new ProgressListener() {
			void progressChanged(ProgressEvent event) {
				// TODO うまい感じでprogressをログ表示できないか
//				System.out.printf("%d%%%n", (int) upload.progress.percentTransferred)
//				if (event.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
//					println("Upload completed.")
//				}
			}
		})
		upload.waitForCompletion()
		resourceUrl = s3.getResourceUrl(bucketName, key)
		println "upload completed: $resourceUrl"
	}
}

class AmazonS3FileDeleteTask extends DefaultTask {
	
	{
		description = 'Delete file from the Amazon S3 bucket.'
		group = 'AWS'
	}

	def String bucketName
	
	def String key
	
	@TaskAction
	def delete() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		
		def AmazonS3Client s3 = project.aws.s3
		println "deleting... ${bucketName}/${key}"
		s3.deleteObject(bucketName, key)
	}
}

class AmazonS3DeleteAllFilesTask extends DefaultTask {
	
	{
		description = 'Delete all files on S3 bucket.'
		group = 'AWS'
	}

	def String bucketName
	
	def String prefix = ''
	
	@TaskAction
	def delete() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		
		def AmazonS3Client s3 = project.aws.s3
		def prefix = this.prefix.startsWith('/') ? this.prefix.substring(1) : this.prefix
		println "deleting... ${bucketName}/${prefix}"
		
		def List<S3ObjectSummary> objectSummaries
		while ((objectSummaries = s3.listObjects(bucketName, prefix).objectSummaries).isEmpty() == false) {
			objectSummaries.each { S3ObjectSummary os ->
				println "deleting... s3://${bucketName}/${os.key}"
				s3.deleteObject(bucketName, os.key)
			}
		}
	}
}
