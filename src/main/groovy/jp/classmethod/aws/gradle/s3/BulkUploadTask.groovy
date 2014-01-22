package jp.classmethod.aws.gradle.s3

import com.amazonaws.services.s3.AmazonS3Client
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction;


class BulkUploadTask extends DefaultTask {

	String bucketName
	
	String prefix
	
	FileTree source
	
	@TaskAction
	def upload() {
		String prefix = this.prefix.startsWith('/') ? this.prefix.substring(1) : this.prefix
		prefix += this.prefix.endsWith('/') ? '' : '/'
		
		AmazonS3Client s3 = project.aws.s3
		
		println "uploading... ${source} to s3://${bucketName}/${prefix}"
		source.visit { FileTreeElement element ->
			if (element.isDirectory() == false) {
				println " => s3://${bucketName}/${prefix}${element.relativePath}"
				s3.putObject(bucketName, prefix + element.relativePath, element.file)
			}
		}
	}
}
