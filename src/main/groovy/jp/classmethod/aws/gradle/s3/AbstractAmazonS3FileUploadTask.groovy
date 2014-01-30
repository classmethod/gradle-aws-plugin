package jp.classmethod.aws.gradle.s3

import java.util.List;

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

abstract class AbstractAmazonS3FileUploadTask extends DefaultTask {
	
	String bucketName
	
	def key
	
	File file
	
	boolean overwrite = true
	
	// == after did work
	
	String resourceUrl
	
	
	String getKey() {
		if (key instanceof String) return key
		if (key instanceof Closure<String>) return key.call()
		return key.toString()
	}
	
	boolean exists() {
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		try {
			ObjectMetadata objectMetadata = s3.getObjectMetadata(bucketName, getKey())
			return true
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() != 404) {
				throw e
			}
		}
		return false
	}
}
