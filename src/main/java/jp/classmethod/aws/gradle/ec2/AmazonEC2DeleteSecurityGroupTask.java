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
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonEC2DeleteSecurityGroupTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String groupName;
	
	@Getter
	@Setter
	private String groupId;
	
	
	public AmazonEC2DeleteSecurityGroupTask() {
		super("AWS", "Delete security group.");
	}
	
	@TaskAction
	public void authorizeIngress() {
		// to enable conventionMappings feature
		String groupName = getGroupName();
		String groupId = getGroupId();
		
		AmazonEC2PluginExtension ext = getPluginExtension(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		if (groupName == null && groupId == null) {
			throw new GradleException("groupName nor groupId is not specified");
		}
		
		try {
			ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest()
				.withGroupId(groupId)
				.withGroupName(groupName));
		} catch (AmazonServiceException e) {
			if (e.getErrorCode().equals("InvalidPermission.Duplicate")) {
				getLogger().warn(e.getMessage());
			} else {
				throw e;
			}
		}
	}
}
