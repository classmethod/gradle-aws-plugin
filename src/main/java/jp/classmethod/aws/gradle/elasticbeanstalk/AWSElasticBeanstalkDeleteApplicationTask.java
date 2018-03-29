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
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkDeleteApplicationTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String appName;
	
	
	public AWSElasticBeanstalkDeleteApplicationTask() {
		super("AWS", "Delete ElasticBeanstalk Application.");
	}
	
	@TaskAction
	public void deleteApplication() {
		// to enable conventionMappings feature
		String appName = getAppName();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		eb.deleteApplication(new DeleteApplicationRequest(appName));
		getLogger().info("application " + appName + " deleted");
	}
}
