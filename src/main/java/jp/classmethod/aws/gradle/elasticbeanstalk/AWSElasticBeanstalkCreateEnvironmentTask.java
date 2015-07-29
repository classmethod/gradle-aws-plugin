/*
 * Copyright 2013-2015 Classmethod, Inc.
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

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;

public class AWSElasticBeanstalkCreateEnvironmentTask extends ConventionTask {

	@Getter @Setter
	private String appName;

	@Getter @Setter
	private String envName;

	@Getter @Setter
	private String envDesc = "";

	@Getter @Setter
	private String cnamePrefix = java.util.UUID.randomUUID().toString();

	@Getter @Setter
	private String templateName;

	@Getter @Setter
	private String versionLabel;

	@Getter @Setter
	private Tier tier = Tier.WebServer;

	public AWSElasticBeanstalkCreateEnvironmentTask(){
		setDescription("Create/Migrate ElasticBeanstalk Environment."); 
		setGroup("AWS");
	}
	
	@TaskAction
	public void createEnvironment() {
		// to enable conventionMappings feature
		String appName = getAppName();
		String envName = getEnvName();
		String envDesc = getEnvDesc();
		String cnamePrefix = getCnamePrefix();
		String templateName = getTemplateName();
		String versionLabel = getVersionLabel();
		Tier tier = getTier();
		
		AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();

		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
				.withApplicationName(appName)
				.withEnvironmentNames(envName)
				.withIncludeDeleted(false));

		if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
			CreateEnvironmentRequest req = new CreateEnvironmentRequest()
					.withApplicationName(appName)
					.withEnvironmentName(envName)
					.withDescription(envDesc)
					.withTemplateName(templateName)
					.withVersionLabel(versionLabel)
					.withTier(tier.toEnvironmentTier());
			if (tier == Tier.WebServer) {
				req.withCNAMEPrefix(cnamePrefix);
			}
			CreateEnvironmentResult result = eb.createEnvironment(req);
			getLogger().info("environment "+envName+" @ "+appName+" ("+result.getEnvironmentId()+") created");
		} else {
			String environmentId = der.getEnvironments().get(0).getEnvironmentId();

			eb.updateEnvironment(new UpdateEnvironmentRequest()
					.withEnvironmentId(environmentId)
					.withEnvironmentName(envName)
					.withDescription(envDesc)
					.withTemplateName(templateName)
					.withVersionLabel(versionLabel)
					.withTier(tier.toEnvironmentTier()));
			getLogger().info("environment "+envName+" @ "+appName+" ("+environmentId+") updated");
		}
	}
}
