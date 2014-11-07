/*
 * Copyright 2013-2014 Classmethod, Inc.
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
package jp.classmethod.aws.gradle.ec2

import java.util.List

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.ec2.*
import com.amazonaws.services.ec2.model.*


class AmazonEC2AuthorizeSecurityGroupEgressTask extends AbstractAmazonEC2SecurityGroupPermissionTask {
	
	{
		description 'Authorize security group ingress.'
		group = 'AWS'
	}
	
	String groupId
	
	def ipPermissions
	
	@TaskAction
	def createApplication() {
		// to enable conventionMappings feature
		String groupId = getGroupId()
		def ipPermissions = getIpPermissions()
	
		if (! groupId) throw new GradleException("groupId is not specified")
		if (! ipPermissions) throw new GradleException("ipPermissions is not specified")
		
		AmazonEC2PluginExtension ext = project.extensions.getByType(AmazonEC2PluginExtension)
		AmazonEC2 ec2 = ext.ec2
		
		try {
			ec2.authorizeSecurityGroupEgress(new AuthorizeSecurityGroupEgressRequest()
				.withGroupId(groupId)
				.withIpPermissions(parse(toCollection(ipPermissions))))
		} catch (AmazonServiceException e) {
			if (e.getErrorCode() == 'InvalidPermission.Duplicate') {
				logger.warn(e.getMessage())
			} else {
				throw e
			}
		}
	}
}
