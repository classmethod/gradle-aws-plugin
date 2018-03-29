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
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;

public class AmazonEC2RevokeSecurityGroupIngressTask extends AbstractAmazonEC2SecurityGroupPermissionTask {
	
	@Getter
	@Setter
	private String groupId;
	
	@Getter
	@Setter
	private String groupName;
	
	@Getter
	@Setter
	private Object ipPermissions;
	
	
	public AmazonEC2RevokeSecurityGroupIngressTask() {
		super("AWS", "Revoke security group ingress.");
	}
	
	@TaskAction
	public void revokeIngress() {
		// to enable conventionMappings feature
		String groupId = getGroupId();
		String groupName = getGroupName();
		Object ipPermissions = getIpPermissions();
		
		if (groupId == null && groupName == null) {
			throw new GradleException("groupId nor groupName is not specified");
		}
		if (ipPermissions == null) {
			throw new GradleException("ipPermissions is not specified");
		}
		
		AmazonEC2PluginExtension ext = getPluginExtension(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		try {
			ec2.revokeSecurityGroupIngress(new RevokeSecurityGroupIngressRequest()
				.withGroupId(groupId)
				.withGroupName(groupName)
				.withIpPermissions(parse(ipPermissions)));
		} catch (AmazonServiceException e) {
			if (e.getErrorCode().equals("InvalidPermission.NotFound")) {
				getLogger().warn(e.getMessage());
			} else {
				throw e;
			}
		}
	}
}
