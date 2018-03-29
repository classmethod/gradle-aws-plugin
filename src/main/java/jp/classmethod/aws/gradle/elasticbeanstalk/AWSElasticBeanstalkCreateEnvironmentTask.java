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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.Tag;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkCreateEnvironmentTask extends BaseAwsTask { // NOPMD
	
	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private String envName;
	
	@Getter
	@Setter
	private String envDesc = "";
	
	@Getter
	@Setter
	private String cnamePrefix = java.util.UUID.randomUUID().toString();
	
	@Getter
	@Setter
	private String templateName;
	
	@Getter
	@Setter
	private String versionLabel;
	
	@Getter
	@Setter
	private Tier tier = Tier.WebServer;
	
	@Getter
	@Setter
	private Map<String, String> tags = new HashMap<String, String>();
	
	
	public AWSElasticBeanstalkCreateEnvironmentTask() {
		super("AWS", "Create/Migrate ElasticBeanstalk Environment.");
	}
	
	@TaskAction
	public void createEnvironment() { // NOPMD
		// to enable conventionMappings feature
		String appName = getAppName();
		String envName = getEnvName();
		String envDesc = getEnvDesc();
		String cnamePrefix = getCnamePrefix();
		String templateName = getTemplateName();
		String versionLabel = getVersionLabel();
		Tier tier = getTier();
		Map<String, String> tags = getTags();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(appName)
			.withEnvironmentNames(envName)
			.withIncludeDeleted(false));
		
		List<Tag> ebTags = tags
			.entrySet()
			.stream()
			.map(entry -> {
				Tag t = new Tag();
				t.setKey(entry.getKey());
				t.setValue(entry.getValue());
				return t;
			})
			.collect(Collectors.toList());
		
		if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
			CreateEnvironmentRequest req = new CreateEnvironmentRequest()
				.withApplicationName(appName)
				.withEnvironmentName(envName)
				.withDescription(envDesc)
				.withTemplateName(templateName)
				.withVersionLabel(versionLabel);
			
			if (tier != null) {
				req.withTier(tier.toEnvironmentTier());
				if (tier == Tier.WebServer) {
					req.withCNAMEPrefix(cnamePrefix);
				}
			}
			
			if (ebTags != null && !ebTags.isEmpty()) {
				req.withTags(ebTags);
			}
			
			CreateEnvironmentResult result = eb.createEnvironment(req);
			getLogger().info("environment {} @ {} ({}) created", envName, appName, result.getEnvironmentId());
		} else {
			String environmentId = der.getEnvironments().get(0).getEnvironmentId();
			
			// Only these two values are required to deploy the a application
			UpdateEnvironmentRequest req = new UpdateEnvironmentRequest()
				.withEnvironmentId(environmentId)
				.withVersionLabel(versionLabel);
			
			// All other variables are optional and refer to the environment
			if (isNotBlank(envName)) {
				req.withEnvironmentName(envName);
			}
			if (isNotBlank(envDesc)) {
				req.withDescription(envDesc);
			}
			if (isNotBlank(templateName)) {
				req.withTemplateName(templateName);
			}
			
			eb.updateEnvironment(req);
			
			getLogger().info("environment {} @ {} ({}) updated", envName, appName, environmentId);
		}
	}
	
	// simple helper method to not include apache commons lang's StringUtils only for this
	private boolean isNotBlank(String str) {
		return str != null && !str.trim().isEmpty(); // NOPMD
	}
}
