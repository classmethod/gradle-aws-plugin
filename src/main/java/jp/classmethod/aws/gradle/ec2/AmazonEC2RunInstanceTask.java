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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.google.common.base.Strings;

public class AmazonEC2RunInstanceTask extends ConventionTask {
	
	@Getter
	@Setter
	private String ami;
	
	@Getter
	@Setter
	private String keyName;
	
	@Getter
	@Setter
	private List<String> securityGroupIds;
	
	@Getter
	@Setter
	private List<String> securityGroups;
	
	@Getter
	@Setter
	private String userData;
	
	@Getter
	@Setter
	private String instanceType;
	
	@Getter
	@Setter
	private String subnetId;
	
	@Getter
	@Setter
	private String iamInstanceProfileName;
	
	@Getter
	private RunInstancesResult runInstancesResult;
	
	
	public AmazonEC2RunInstanceTask() {
		setDescription("Run EC2 instance.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void runInstance() {
		// to enable conventionMappings feature
		
		String ami = getAmi();
		String keyName = getKeyName();
		List<String> securityGroupIds = getSecurityGroupIds();
		String userData = getUserData();
		String instanceType = getInstanceType();
		String subnetId = getSubnetId();
		String iamInstanceProfileName = getIamInstanceProfileName();
		List<String> securityGroups = getSecurityGroups();
		
		if (ami == null) {
			throw new GradleException("AMI ID is required");
		}
		
		AmazonEC2PluginExtension ext = getProject().getExtensions().getByType(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		RunInstancesRequest request = new RunInstancesRequest()
			.withImageId(ami)
			.withKeyName(keyName)
			.withMinCount(1)
			.withMaxCount(1)
			.withSecurityGroupIds(securityGroupIds)
			.withSecurityGroups(securityGroups)
			.withInstanceType(instanceType)
			.withSubnetId(subnetId)
			.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(iamInstanceProfileName));
		if (Strings.isNullOrEmpty(this.userData) == false) {
			request.setUserData(new String(Base64.getEncoder()
				.encode(userData.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
		}
		runInstancesResult = ec2.runInstances(request);
		String instanceIds = runInstancesResult.getReservation().getInstances().stream()
			.map(i -> i.getInstanceId())
			.collect(Collectors.joining(", "));
		getLogger().info("Run EC2 instance requested: {}", instanceIds);
	}
}
