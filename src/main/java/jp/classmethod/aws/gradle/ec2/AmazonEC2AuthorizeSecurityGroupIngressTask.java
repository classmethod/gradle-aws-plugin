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

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;

public class AmazonEC2AuthorizeSecurityGroupIngressTask extends AbstractAmazonEC2SecurityGroupPermissionTask {
	
	@Getter
	@Setter
	private String groupId;
	
	@Getter
	@Setter
	private String groupName;
	
	@Getter
	@Setter
	private Object ipPermissions;
	
	
	public AmazonEC2AuthorizeSecurityGroupIngressTask() {
		setDescription("Authorize security group ingress.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void authorizeIngress() {
		// to enable conventionMappings feature
		String groupId = getGroupId();
		String groupName = getGroupName();
		Object ipPermissions = getIpPermissions();
		
		checkGroupsAndIpPermissions(groupId, groupName, ipPermissions);
		
		AmazonEC2PluginExtension ext = getProject().getExtensions().getByType(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		try {
			ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest()
				.withGroupId(groupId)
				.withGroupName(groupName)
				.withIpPermissions(parse(ipPermissions)));
		} catch (AmazonServiceException e) {
			if (e.getErrorCode().equals("InvalidPermission.Duplicate")) {
				getLogger().warn(e.getMessage());
			} else {
				throw e;
			}
		}
	}
}
