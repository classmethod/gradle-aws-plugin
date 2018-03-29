/*
 * Copyright 2015-2016 the original author or authors.
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
package jp.classmethod.aws.gradle.s3;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

public class AmazonS3ProgressiveFileUploadTask extends AbstractAmazonS3FileUploadTask {
	
	public AmazonS3ProgressiveFileUploadTask() {
		super("AWS", "Upload file to the Amazon S3 bucket.");
	}
	
	@TaskAction
	public void upload() throws InterruptedException {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String key = getKey();
		File file = getFile();
		
		if (bucketName == null) {
			throw new GradleException("bucketName is not specified");
		}
		if (key == null) {
			throw new GradleException("key is not specified");
		}
		if (file == null) {
			throw new GradleException("file is not specified");
		}
		if (file.isFile() == false) {
			throw new GradleException("file must be regular file");
		}
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		TransferManager s3mgr = TransferManagerBuilder.standard().withS3Client(s3).build();
		getLogger().info("Uploading... s3://{}/{}", bucketName, key);
		
		Upload upload = s3mgr.upload(new PutObjectRequest(getBucketName(), getKey(), getFile())
			.withMetadata(getObjectMetadata()));
		upload.addProgressListener(new ProgressListener() {
			
			public void progressChanged(ProgressEvent event) {
				getLogger().info("  {}% uploaded", upload.getProgress().getPercentTransferred());
			}
		});
		upload.waitForCompletion();
		setResourceUrl(s3.getUrl(bucketName, key).toString());
		getLogger().info("Upload completed: {}", getResourceUrl());
	}
}
