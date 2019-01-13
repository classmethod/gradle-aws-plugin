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
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;

public class BucketWebsiteConfigurationTask extends ConventionTask {
	@Getter
	@Setter
	private String bucketName;
	
	@Getter
	@Setter
	private String indexDoc;
	
	@Getter
	@Setter
	private String errorDoc;
	
	
	public BucketWebsiteConfigurationTask() {
		setDescription("Enable S3 bucket as public website");
		setGroup("AWS");
	}
	
	@TaskAction
	public void applyWebsiteConfiguration() {
		BucketWebsiteConfiguration websiteConfig = null;
		
		if (getIndexDoc() == null) {
			websiteConfig = new BucketWebsiteConfiguration();
		} else if (errorDoc == null) {
			websiteConfig = new BucketWebsiteConfiguration(getIndexDoc());
		} else {
			websiteConfig = new BucketWebsiteConfiguration(getIndexDoc(), getErrorDoc());
		}
		
		AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
		final AmazonS3 s3 = ext.getClient();
		
		try {
			s3.setBucketWebsiteConfiguration(getBucketName(), websiteConfig);
		} catch (AmazonServiceException ex) {
			throw new GradleException("Failed to set website configuration for bucket " + getBucketName(), ex);
		}
	}
}
