/*
 * Copyright 2013-2016 Classmethod, Inc.
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
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;


public class CreateBucketTask extends ConventionTask {
	
	@Getter @Setter
	public String bucketName;
	
	@Getter @Setter
	public boolean ifNotExists;
	
	public CreateBucketTask() {
		setDescription("Create the Amazon S3 bucket.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void createBucket() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();

		if (bucketName == null) throw new GradleException("bucketName is not specified");
		
		AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		if (isIfNotExists() == false || exists(s3) == false) {
			s3.createBucket(bucketName);
			getLogger().info("S3 Bucket '{}' created", bucketName);
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
