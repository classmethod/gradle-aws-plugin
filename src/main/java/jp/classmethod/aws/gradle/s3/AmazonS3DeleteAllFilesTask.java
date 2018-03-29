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

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonS3DeleteAllFilesTask extends BaseAwsTask {
	
	@Getter
	@Setter
	public String bucketName;
	
	@Getter
	@Setter
	public String prefix = "";
	
	
	public AmazonS3DeleteAllFilesTask() {
		super("AWS", "Delete all files in the S3 bucket.");
	}
	
	@TaskAction
	public void delete() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String prefix = getPrefix();
		
		if (bucketName == null) {
			throw new GradleException("bucketName is not specified");
		}
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		if (prefix.startsWith("/")) {
			prefix = prefix.substring(1);
		}
		
		getLogger().info("Delete s3://{}/{}*", bucketName, prefix);
		
		List<S3ObjectSummary> objectSummaries;
		while ((objectSummaries = s3.listObjects(bucketName, prefix).getObjectSummaries()).isEmpty() == false) {
			objectSummaries.forEach(os -> {
				getLogger().info("  Deleting... s3://{}/{}", bucketName, os.getKey());
				s3.deleteObject(bucketName, os.getKey());
			});
		}
	}
}
