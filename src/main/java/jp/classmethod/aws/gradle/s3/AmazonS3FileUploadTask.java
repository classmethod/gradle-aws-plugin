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
import java.io.IOException;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class AmazonS3FileUploadTask extends AbstractAmazonS3FileUploadTask {
	
	public AmazonS3FileUploadTask() {
		super("AWS", "Upload file to the Amazon S3 bucket.");
	}
	
	@TaskAction
	public void upload() throws IOException {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String key = getKey();
		File file = getFile();
		String kmsKeyId = getKmsKeyId();
		
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
		
		// metadata will be null iff the object does not exist
		ObjectMetadata metadata = existingObjectMetadata();
		
		if (metadata == null || (isOverwrite() || metadata.getETag().equals(md5()) == false)) {
			getLogger().info("uploading... " + bucketName + "/" + key);
			if (kmsKeyId == null) {
				putObject(bucketName, key, file, s3);
			} else {
				putObjectWithKmsSupport(bucketName, key, file, kmsKeyId, s3);
			}
			getLogger().info("upload completed: " + ((AmazonS3Client) s3).getResourceUrl(bucketName, key));
		} else {
			getLogger().info("s3://{}/{} already exists with matching md5 sum -- skipped", bucketName, key);
		}
		setResourceUrl(s3.getUrl(bucketName, key).toString());
	}
	
	private String md5() throws IOException {
		return Files.hash(getFile(), Hashing.md5()).toString();
	}
	
	private void putObject(String bucketName, String key, File file, AmazonS3 s3) {
		s3.putObject(new PutObjectRequest(bucketName, key, file)
			.withMetadata(getObjectMetadata()));
	}
	
	private void putObjectWithKmsSupport(String bucketName, String key, File file, String kmsKeyId, AmazonS3 s3) {
		s3.putObject(new PutObjectRequest(bucketName, key, file)
			.withMetadata(getObjectMetadata())
			.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsKeyId)));
	}
}
