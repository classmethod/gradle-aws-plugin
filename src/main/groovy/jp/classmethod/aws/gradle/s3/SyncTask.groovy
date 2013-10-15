package jp.classmethod.aws.gradle.s3

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*

class BulkUploadTask extends DefaultTask {

	def String bucketName		def String prefix		def FileTree source		@TaskAction
	def upload() {
		def prefix = this.prefix.startsWith('/') ? this.prefix.substring(1) : this.prefix
		prefix += this.prefix.endsWith('/') ? '' : '/'
		
		def AmazonS3Client s3 = project.aws.s3
		
		println "uploading... ${source} to s3://${bucketName}/${prefix}"
		source.visit { FileTreeElement element ->
			if (element.isDirectory() == false) {
				println " => s3://${bucketName}/${prefix}${element.relativePath}"
				s3.putObject(bucketName, prefix + element.relativePath, element.file)
			}
		}
	}
}

class SyncTask extends DefaultTask {
	
	def String bucketName
	
	def String prefix
	
	def File source
	
	@TaskAction
	def uploadAction() {
		def prefix = this.prefix.startsWith('/') ? this.prefix.substring(1) : this.prefix
		prefix += this.prefix.endsWith('/') ? '' : '/'
		
		upload(prefix)
		delete(prefix)
	}
	
	private String upload(String prefix) {
		def AmazonS3Client s3 = project.aws.s3
		println "uploading... ${source} to s3://${bucketName}/${prefix}"
		project.fileTree(source).visit { FileTreeElement element ->
			if (element.isDirectory() == false) {
				println " => s3://${bucketName}/${prefix}${element.relativePath}"
				s3.putObject(bucketName, prefix + element.relativePath, element.file)
			}
		}
	}
	
	private delete(String prefix) {
		def AmazonS3Client s3 = project.aws.s3
		def String pathPrefix = source.toString()
		pathPrefix += pathPrefix.endsWith('/') ? '' : '/'
		s3.listObjects(bucketName, prefix).objectSummaries.each { S3ObjectSummary os ->
			def File f = project.file(pathPrefix + os.key.substring(prefix.length()))
			if (f.exists() == false) {
				println "deleting... s3://${bucketName}/${os.key}"
				s3.deleteObject(bucketName, os.key)
			}
		}
	}
}
