package jp.classmethod.aws.gradle.s3

import java.util.List;

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction


class AmazonS3FileUploadTask extends AbstractAmazonS3FileUploadTask {
	
	{
		description = 'Upload file to the Amazon S3 bucket.'
		group = 'AWS'
	}

	@TaskAction
	def upload() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		if (! file) throw new GradleException("file is not specified")
		
		if (overwrite || exists() == false) {
			AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
			AmazonS3 s3 = ext.s3
					
			println "uploading... ${bucketName}/${getKey()}"
			resourceUrl = ((AmazonS3Client) s3).getResourceUrl(bucketName, getKey())
			s3.putObject(bucketName, getKey(), file)
			println "upload completed: $resourceUrl"
		} else {
			println "${bucketName}/${getKey()} is already exists -- skipped"
		}
	}
}
