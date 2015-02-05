/*
 * Copyright 2013-2014 Classmethod, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.s3

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*


class AmazonS3ProgressiveFileUploadTask extends AbstractAmazonS3FileUploadTask {
	
	{
		description 'Upload war file to the Amazon S3 bucket.'
		group = 'AWS'
	}
	
	@TaskAction
	def upload() {
		// to enable conventionMappings feature
		String bucketName = getBucketName()
		String key = getKey()
		File file = getFile()

		if (! bucketName) throw new GradleException("bucketName is not specified")
		if (! key) throw new GradleException("key is not specified")
		if (! file) throw new GradleException("file is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		TransferManager s3mgr = new TransferManager(s3)
		logger.info "uploading... $bucketName/$key"
		
		Upload upload = s3mgr.upload(new PutObjectRequest(getBucketName(), getKey(), getFile())
			.withMetadata(getObjectMetadata()))
		upload.addProgressListener(new ProgressListener() {
			void progressChanged(ProgressEvent event) {
				// TODO progress logging
//				System.out.printf("%d%%%n", (int) upload.progress.percentTransferred)
//				if (event.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
//					logger.info("Upload completed.")
//				}
			}
		})
		upload.waitForCompletion()
		resourceUrl = ((AmazonS3Client) s3).getResourceUrl(bucketName, key)
		logger.info "upload completed: $resourceUrl"
	}
}
