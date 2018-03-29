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

import com.amazonaws.services.s3.AmazonS3;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonS3FileDeleteTask extends BaseAwsTask {
	
	public AmazonS3FileDeleteTask() {
		super("AWS", "Delete file from the Amazon S3 bucket.");
	}
	
	
	@Getter
	@Setter
	String bucketName;
	
	@Getter
	@Setter
	String key;
	
	
	@TaskAction
	public void delete() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String key = getKey();
		
		if (bucketName == null) {
			throw new GradleException("bucketName is not specified");
		}
		if (key == null) {
			throw new GradleException("key is not specified");
		}
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		getLogger().info("deleting... " + bucketName + "/" + key);
		s3.deleteObject(bucketName, key);
	}
}
