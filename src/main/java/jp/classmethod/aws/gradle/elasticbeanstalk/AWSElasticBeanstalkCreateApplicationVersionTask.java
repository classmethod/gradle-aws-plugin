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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkCreateApplicationVersionTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private String versionLabel;
	
	@Getter
	@Setter
	private String bucketName;
	
	@Getter
	@Setter
	private String key;
	
	
	public AWSElasticBeanstalkCreateApplicationVersionTask() {
		super("AWS", "Create/Migrate ElasticBeanstalk Application Version.");
	}
	
	@TaskAction
	public void createVersion() {
		// to enable conventionMappings feature
		String appName = getAppName();
		String versionLabel = getVersionLabel();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		try {
			eb.createApplicationVersion(new CreateApplicationVersionRequest()
				.withApplicationName(appName)
				.withVersionLabel(versionLabel)
				.withSourceBundle(new S3Location(getBucketName(), getKey())));
			getLogger().info("version " + versionLabel + " @ " + appName + " created");
		} catch (AmazonServiceException e) {
			if (!e.getMessage().contains("already exists.")) {
				throw e;
			}
			getLogger().warn("version " + versionLabel + " @ " + appName + " already exists.");
		}
	}
}
