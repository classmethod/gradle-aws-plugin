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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class DeleteBucketTask extends BaseAwsTask {
	
	@Getter
	@Setter
	String bucketName;
	
	@Getter
	@Setter
	boolean ifExists;
	
	@Getter
	@Setter
	boolean deleteObjects;
	
	
	public DeleteBucketTask() {
		super("AWS", "Create the Amazon S3 bucket.");
	}
	
	@TaskAction
	public void deleteBucket() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		boolean ifExists = isIfExists();
		
		if (bucketName == null) {
			throw new GradleException("bucketName is not specified");
		}
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		if (ifExists == false || exists(s3)) {
			if (deleteObjects) {
				getLogger().info("Delete all S3 objects in bucket [{}]", bucketName);
				ObjectListing objectListing = s3.listObjects(bucketName);
				while (objectListing.getObjectSummaries().isEmpty() == false) {
					objectListing.getObjectSummaries().forEach(summary -> {
						getLogger().info(" => delete s3://{}/{}", bucketName, summary.getKey());
						s3.deleteObject(bucketName, summary.getKey());
					});
					objectListing = s3.listNextBatchOfObjects(objectListing);
				}
			}
			s3.deleteBucket(bucketName);
			getLogger().info("S3 bucket {} is deleted", bucketName);
		} else {
			getLogger().debug("S3 bucket {} does not exist", bucketName);
		}
	}
	
	private boolean exists(AmazonS3 s3) {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		
		try {
			s3.getBucketLocation(bucketName);
			return true;
		} catch (AmazonClientException e) {
			return false;
		}
	}
}
