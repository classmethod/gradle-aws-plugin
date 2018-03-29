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
import com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkDeleteConfigurationTemplateTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String applicationName;
	
	@Getter
	@Setter
	private String templateName;
	
	
	public AWSElasticBeanstalkDeleteConfigurationTemplateTask() {
		super("AWS", "Delete ElasticBeanstalk Configuration Templates.");
	}
	
	@TaskAction
	public void deleteTemplate() {
		// to enable conventionMappings feature
		String applicationName = getApplicationName();
		String templateName = getTemplateName();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
			.withApplicationName(applicationName)
			.withTemplateName(templateName));
		
		getLogger().info("configuration template " + templateName + " @ " + applicationName + " deleted");
	}
}
