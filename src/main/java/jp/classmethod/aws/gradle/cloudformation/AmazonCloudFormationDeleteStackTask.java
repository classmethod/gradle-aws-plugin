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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonCloudFormationDeleteStackTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String stackName;
	
	
	public AmazonCloudFormationDeleteStackTask() {
		super("AWS", "Delete cfn stack.");
	}
	
	@TaskAction
	public void deleteStack() {
		// to enable conventionMappings feature
		String stackName = getStackName();
		
		if (stackName == null) {
			throw new GradleException("stackName is not specified");
		}
		
		AmazonCloudFormationPluginExtension ext = getPluginExtension(AmazonCloudFormationPluginExtension.class);
		AmazonCloudFormation cfn = ext.getClient();
		
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName));
		getLogger().info("delete stack " + stackName + " requested");
	}
}
