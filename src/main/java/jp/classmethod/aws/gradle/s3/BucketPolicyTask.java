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

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.s3.AmazonS3;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class BucketPolicyTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String bucketName;
	
	@Getter
	@Setter
	private Policy policy;
	
	
	public BucketPolicyTask() {
		super("AWS", "Set an S3 bucket policy");
	}
	
	@TaskAction
	public void applyBucketPolicy() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		Policy policy = getPolicy();
		
		if (bucketName == null) {
			throw new GradleException("bucketName is not specified");
		}
		if (policy == null) {
			throw new GradleException("policy is not specified");
		}
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		String policyJson = policy.toJson();
		getLogger().info("Setting s3://{} bucket policy to {}", bucketName, policyJson);
		s3.setBucketPolicy(bucketName, policy.toJson());
	}
}
