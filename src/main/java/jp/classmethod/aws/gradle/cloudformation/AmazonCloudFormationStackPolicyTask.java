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
package jp.classmethod.aws.gradle.cloudformation;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.SetStackPolicyRequest;
import com.google.common.base.Strings;

public class AmazonCloudFormationStackPolicyTask extends ConventionTask {
	
	@Getter
	@Setter
	private String stackName;
	
	@Getter
	@Setter
	private String cfnStackPolicyUrl;
	
	@Getter
	@Setter
	private File cfnStackPolicyFile;
	
	
	public AmazonCloudFormationStackPolicyTask() {
		setDescription("Set CloudFormation policy");
		setGroup("AWS");
	}
	
	@TaskAction
	public void setStackPolicy() throws IOException {
		AmazonCloudFormationPluginExtension ext =
				getProject().getExtensions().getByType(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		String stackName = getStackName();
		String cfnStackPolicyUrl = getCfnStackPolicyUrl();
		File cfnStackPolicyFile = getCfnStackPolicyFile();
		
		SetStackPolicyRequest request = new SetStackPolicyRequest().withStackName(stackName);
		
		if (!Strings.isNullOrEmpty(cfnStackPolicyUrl)) {
			request.setStackPolicyURL(cfnStackPolicyUrl);
			getLogger().info(String.format(Locale.ENGLISH, "Setting stack policy for stack %s using URL %s", stackName,
					cfnStackPolicyUrl));
			
		} else if (cfnStackPolicyFile != null) {
			request.setStackPolicyBody(
					FileUtils.readFileToString(cfnStackPolicyFile));
			getLogger().info(String.format(Locale.ENGLISH,
					"Setting stack policy for stack " + stackName + " using file " + cfnStackPolicyFile));
			
		}
		
		cfn.setStackPolicy(request);
	}
}
