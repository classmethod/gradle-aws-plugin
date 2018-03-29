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
package jp.classmethod.aws.gradle.elasticbeanstalk;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;

import jp.classmethod.aws.gradle.s3.AmazonS3FileUploadTask;

public class AWSElasticBeanstalkUploadBundleTask extends AmazonS3FileUploadTask {
	
	@Getter
	@Setter
	private String extension = "zip";
	
	@Getter
	private String versionLabel;
	
	
	public AWSElasticBeanstalkUploadBundleTask() {
		setDescription("Upload Elastic Beanstalk application bundle file to S3.");
	}
	
	@Override
	public void upload() throws IOException {
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'_'HHmmss", Locale.ENGLISH);
		df.setTimeZone(TimeZone.getDefault());
		Project project = getProject();
		versionLabel = String.format(Locale.ENGLISH, "%s-%s", project.getVersion().toString(), df.format(new Date()));
		
		String artifactId = project.property("artifactId").toString();
		
		setBucketName(eb.createStorageLocation().getS3Bucket());
		setKey(String.format(Locale.ENGLISH, "eb-apps/%s/%s-%s.%s", new Object[] {
			artifactId,
			artifactId,
			versionLabel,
			extension
		}));
		
		super.upload();
	}
}
