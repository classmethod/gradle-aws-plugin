package jp.classmethod.aws.gradle.s3

import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.Upload
import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction;


class AmazonS3ProgressiveFileUploadTask extends DefaultTask {
	
	{
		description 'Upload war file to the Amazon S3 bucket.'
		group = 'AWS'
	}
	
	String bucketName
	
	String key
	
	File file
	
	// == after did work
	
	String resourceUrl
	
	@TaskAction
	def upload() {
		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		if (! file) throw new GradleException("file is not specified")
		
		AmazonS3Client s3 = project.aws.s3
		TransferManager s3mgr = new TransferManager(s3)
		println "uploading... ${bucketName}/${key}"
		Upload upload = s3mgr.upload(bucketName, key, file)
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
