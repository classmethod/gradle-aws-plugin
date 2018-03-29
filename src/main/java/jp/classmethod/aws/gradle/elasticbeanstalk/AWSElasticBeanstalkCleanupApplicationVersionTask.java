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

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription;
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AWSElasticBeanstalkCleanupApplicationVersionTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private boolean deleteSourceBundle = true;
	
	
	public AWSElasticBeanstalkCleanupApplicationVersionTask() {
		super("AWS", "Cleanup unused SNAPSHOT ElasticBeanstalk Application Version.");
	}
	
	@TaskAction
	public void deleteVersion() {
		// to enable conventionMappings feature
		String appName = getAppName();
		boolean deleteSourceBundle = isDeleteSourceBundle();
		
		AwsBeanstalkPluginExtension ext = getPluginExtension(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(appName));
		List<String> usingVersions = der.getEnvironments().stream()
			.map(EnvironmentDescription::getVersionLabel)
			.collect(Collectors.toList());
		
		DescribeApplicationVersionsResult davr = eb.describeApplicationVersions(new DescribeApplicationVersionsRequest()
			.withApplicationName(appName));
		List<String> versionLabelsToDelete = davr.getApplicationVersions().stream()
			.filter(avd -> usingVersions.contains(avd.getVersionLabel()) == false
					&& avd.getVersionLabel().contains("-SNAPSHOT-"))
			.map(ApplicationVersionDescription::getVersionLabel)
			.collect(Collectors.toList());
		
		versionLabelsToDelete.forEach(versionLabel -> {
			getLogger().info("version " + versionLabel + " deleted");
			eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
				.withApplicationName(appName)
				.withVersionLabel(versionLabel)
				.withDeleteSourceBundle(deleteSourceBundle));
		});
	}
}
