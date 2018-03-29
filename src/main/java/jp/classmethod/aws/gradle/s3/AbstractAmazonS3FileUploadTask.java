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

import lombok.Getter;
import lombok.Setter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public abstract class AbstractAmazonS3FileUploadTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String bucketName;
	
	@Getter
	@Setter
	private String key;
	
	@Getter
	@Setter
	private String kmsKeyId;
	
	@Getter
	@Setter
	private File file;
	
	@Getter
	@Setter
	private ObjectMetadata objectMetadata;
	
	@Getter
	@Setter
	private String resourceUrl;
	
	@Getter
	@Setter
	private boolean overwrite = false;
	
	
	public AbstractAmazonS3FileUploadTask(String group, String description) {
		super(group, description);
	}
	
	protected ObjectMetadata existingObjectMetadata() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String key = getKey();
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		try {
			// to enable conventionMapping, you must reference field via getters
			return s3.getObjectMetadata(bucketName, key);
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() != 404) {
				throw e;
			}
		}
		return null;
	}
	
	protected boolean exists() {
		return existingObjectMetadata() != null;
	}
}
