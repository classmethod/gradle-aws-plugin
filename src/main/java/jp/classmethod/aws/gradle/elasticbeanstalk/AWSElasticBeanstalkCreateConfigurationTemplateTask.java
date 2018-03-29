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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest;
import com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest;
import com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

import groovy.json.JsonParserType;

public class AWSElasticBeanstalkCreateConfigurationTemplateTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private Collection<EbConfigurationTemplateExtension> configurationTemplates = new ArrayList<>();
	
	@Getter
	@Setter
	private String defaultSolutionStackName = "64bit Amazon Linux 2013.09 running Tomcat 7 Java 7";
	
	
	public AWSElasticBeanstalkCreateConfigurationTemplateTask() {
		super("AWS", "Create / Migrate ElasticBeanstalk Configuration Templates.");
	}
	
	@TaskAction
	public void createTemplate() {
		// to enable conventionMappings feature
		String appName = getAppName();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		configurationTemplates.forEach(config -> {
			String templateName = config.getName();
			String templateDesc = config.getDesc();
			String solutionStackName = config.getSolutionStackName() != null ? config.getSolutionStackName()
					: getDefaultSolutionStackName();
			boolean deleteTemplateIfExists = config.isRecreate();
			
			try {
				List<ConfigurationOptionSetting> optionSettings = loadConfigurationOptions(config.getOptionSettings());
				List<ApplicationDescription> existingApps = eb.describeApplications(new DescribeApplicationsRequest()
					.withApplicationNames(appName)).getApplications();
				if (existingApps.isEmpty()) {
					throw new IllegalArgumentException("App with name '" + appName + "' does not exist");
				}
				
				if (existingApps.get(0).getConfigurationTemplates().contains(templateName)) {
					if (deleteTemplateIfExists) {
						eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
							.withApplicationName(appName)
							.withTemplateName(templateName));
						getLogger().info("configuration template {} @ {} deleted", templateName, appName);
					} else {
						eb.updateConfigurationTemplate(new UpdateConfigurationTemplateRequest()
							.withApplicationName(appName)
							.withTemplateName(templateName)
							.withDescription(templateDesc)
							.withOptionSettings(optionSettings));
						getLogger().info("configuration template {} @ {} updated", templateName, appName);
						return;
					}
				}
				
				eb.createConfigurationTemplate(new CreateConfigurationTemplateRequest()
					.withApplicationName(appName)
					.withTemplateName(templateName)
					.withDescription(templateDesc)
					.withSolutionStackName(solutionStackName)
					.withOptionSettings(optionSettings));
				getLogger().info("configuration template {} @ {} created", templateName, appName);
			} catch (IOException e) {
				getLogger().error("IOException", e);
			}
		});
	}
	
	List<ConfigurationOptionSetting> loadConfigurationOptions(String json) {
		List<ConfigurationOptionSetting> options = new ArrayList<>();
		@SuppressWarnings("unchecked")
		Collection<Map<String, Object>> c =
				(Collection<Map<String, Object>>) new groovy.json.JsonSlurper().setType(JsonParserType.LAX)
					.parseText(json);
		c.forEach(it -> options.add(new ConfigurationOptionSetting((String) it.get("Namespace"),
				(String) it.get("OptionName"), (String) it.get("Value"))));
		return options;
	}
}
