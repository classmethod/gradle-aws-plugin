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
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Region;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class CreateBucketTask extends BaseAwsTask {
	
	private static final String AWS_DEFAULT_REGION_NAME = "us-east-1 (default)";
	
	/**
	 * Amazon S3 bucket names are globally unique, regardless of the AWS region in which you create the bucket.
	 * http://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html
	 * See also http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html
	 */
	@Getter
	@Setter
	public String bucketName;
	
	/**
	 * Region identifier. Even empty value is correct.
	 * By default, the bucket is created in the US East (N. Virginia) region.
	 * See http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region for details
	 * Also http://bit.ly/2fxwwt5
	 */
	@Getter
	@Setter
	public String region;
	
	/**
	 * Create bucket only if it does not exists.
	 */
	@Getter
	@Setter
	public boolean ifNotExists;
	
	
	public CreateBucketTask() {
		super("AWS", "Create the Amazon S3 bucket.");
	}
	
	@TaskAction
	public void createBucket() {
		// to enable conventionMappings feature
		final String bucketName = getBucketName();
		final String region = getRegion();
		if (bucketName == null) {
			throw new GradleException("bucketName is not specified");
		}
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		if (isIfNotExists() && exists(s3)) {
			getLogger().info("Bucket already exists and won't be created. Use 'ifNotExists' to override.");
			return;
		}
		
		String regionName = AWS_DEFAULT_REGION_NAME;
		if (region == null) {
			s3.createBucket(bucketName);
		} else {
			regionName = getAwsRegionName(region);
			s3.createBucket(new CreateBucketRequest(bucketName, region));
		}
		getLogger().info("S3 Bucket '{}' created at region '{}'", bucketName, regionName);
	}
	
	private String getAwsRegionName(final String region) {
		try {
			return Region.fromValue(region).toString();
		} catch (IllegalArgumentException e) {
			throw new GradleException(e.getMessage(), e);
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
