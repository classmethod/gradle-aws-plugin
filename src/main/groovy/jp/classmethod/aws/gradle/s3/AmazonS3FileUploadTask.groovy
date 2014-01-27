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

	String bucketName
	
	String key
	
	File file
	
	boolean overwrite = true
	
	// == after did work
	
	String resourceUrl
	
	@TaskAction
	def upload() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		if (! file) throw new GradleException("file is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		println "uploading... ${bucketName}/${key}"
		resourceUrl = ((AmazonS3Client) s3Ext.s3).getResourceUrl(bucketName, key)
		if (overwrite || exists() == false) {
			s3.putObject(bucketName, key, file)
			println "upload completed: $resourceUrl"
		} else {
			println "${bucketName}/${key} is already exists -- skipped"
		}
	}
	
	boolean exists() {
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
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
