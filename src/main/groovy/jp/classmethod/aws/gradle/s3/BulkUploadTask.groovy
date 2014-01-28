package jp.classmethod.aws.gradle.s3

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*


class BulkUploadTask extends DefaultTask {

	String bucketName
	
	String prefix
	
	FileTree source
	
	@TaskAction
	def upload() {
		String prefix = this.prefix.startsWith('/') ? this.prefix.substring(1) : this.prefix
		prefix += this.prefix.endsWith('/') ? '' : '/'
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		println "uploading... ${source} to s3://${bucketName}/${prefix}"
		source.visit { FileTreeElement element ->
			if (element.isDirectory() == false) {
				println " => s3://${bucketName}/${prefix}${element.relativePath}"
				s3.putObject(bucketName, prefix + element.relativePath, element.file)
			}
		}
	}
}
