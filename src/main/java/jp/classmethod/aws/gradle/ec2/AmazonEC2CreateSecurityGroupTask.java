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
package jp.classmethod.aws.gradle.ec2;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonEC2CreateSecurityGroupTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String groupName;
	
	@Getter
	@Setter
	private String groupDescription;
	
	@Getter
	private CreateSecurityGroupResult createSecurityGroupResult;
	
	
	public AmazonEC2CreateSecurityGroupTask() {
		super("AWS", "Create security group.");
	}
	
	@TaskAction
	public void authorizeIngress() {
		// to enable conventionMappings feature
		String groupName = getGroupName();
		String groupDescription = getGroupDescription();
		
		if (groupName == null) {
			throw new GradleException("groupName is not specified");
		}
		
		AmazonEC2PluginExtension ext = getPluginExtension(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		try {
			createSecurityGroupResult = ec2.createSecurityGroup(new CreateSecurityGroupRequest()
				.withGroupName(groupName)
				.withDescription(groupDescription));
		} catch (AmazonServiceException e) {
			if (e.getErrorCode().equals("InvalidPermission.Duplicate")) {
				getLogger().warn(e.getMessage());
			} else {
				throw e;
			}
		}
	}
}
