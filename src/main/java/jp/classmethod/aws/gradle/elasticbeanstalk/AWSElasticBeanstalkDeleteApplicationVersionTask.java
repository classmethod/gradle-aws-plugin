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

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkDeleteApplicationVersionTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String applicationName;
	
	@Getter
	@Setter
	private String versionLabel;
	
	@Getter
	@Setter
	private String bucketName;
	
	@Getter
	@Setter
	private boolean deleteSourceBundle = true;
	
	
	public AWSElasticBeanstalkDeleteApplicationVersionTask() {
		super("AWS", "Delete ElasticBeanstalk Application Version.");
	}
	
	@TaskAction
	public void deleteVersion() {
		// to enable conventionMappings feature
		String applicationName = getApplicationName();
		String versionLabel = getVersionLabel();
		boolean deleteSourceBundle = isDeleteSourceBundle();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
			.withApplicationName(applicationName)
			.withVersionLabel(versionLabel)
			.withDeleteSourceBundle(deleteSourceBundle));
		getLogger().info("version " + versionLabel + " deleted");
	}
}
