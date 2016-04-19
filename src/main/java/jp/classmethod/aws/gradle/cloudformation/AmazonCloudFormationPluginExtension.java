/*
 * Copyright 2013-2016 Classmethod, Inc.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;

public class AmazonCloudFormationPluginExtension {
	
	private static Logger logger = LoggerFactory.getLogger(AmazonCloudFormationPluginExtension.class);
	
	public static final String NAME = "cloudFormation";
	
	@Getter
	private final Project project;
	
	@Getter
	@Setter
	private String profileName;
	
	@Getter
	@Setter
	private String region;
	
	@Getter
	@Setter
	private String stackName;
	
	@Getter
	@Setter
	private Map<?, ?> stackParams = new HashMap<>();
	
	@Getter
	@Setter
	private String templateURL;
	
	@Getter
	@Setter
	private File templateFile;
	
	@Getter
	@Setter
	private String templateBucket;
	
	@Getter
	@Setter
	private String templateKeyPrefix;
	
	@Getter
	@Setter
	private String stackPolicyURL;
	
	@Getter
	@Setter
	private File stackPolicyFile;
	
	@Getter
	@Setter
	private String stackPolicyBucket;
	
	@Getter
	@Setter
	private String stackPolicyKeyPrefix;
	
	@Getter
	@Setter
	private boolean capabilityIam;
	
	@Getter(lazy = true)
	private final AmazonCloudFormation client = initClient();
	
	
	public AmazonCloudFormationPluginExtension(Project project) {
		this.project = project;
	}
	
	private AmazonCloudFormation initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		AmazonCloudFormationClient client = aws.createClient(AmazonCloudFormationClient.class, profileName);
		client.setRegion(aws.getActiveRegion(region));
		return client;
	}
	
	public Optional<Stack> getStack() {
		return getStack(stackName);
	}
	
	public Optional<Stack> getStack(String stackName) {
		if (getProject().getGradle().getStartParameter().isOffline() == false) {
			try {
				DescribeStacksResult describeStacksResult = getClient().describeStacks(new DescribeStacksRequest()
				.withStackName(stackName));
				List<Stack> stacks = describeStacksResult.getStacks();
				if (stacks.isEmpty() == false) {
					return stacks.stream().findAny();
				}
			} catch (AmazonClientException e) {
				logger.debug("describeStacks failed", e);
			}
		}
		return Optional.empty();
	}
	
	public List<Parameter> getStackParameters() {
		return getStackParameters(stackName);
	}
	
	public List<Parameter> getStackParameters(String stackName) {
		if (getProject().getGradle().getStartParameter().isOffline() == false) {
			Optional<Stack> stack = getStack(stackName);
			return stack.map(Stack::getParameters).orElse(Collections.emptyList());
		}
		logger.info("offline mode: return empty parameters");
		return Collections.emptyList();
	}
	
	public List<StackResource> getStackResources() {
		return getStackResources(stackName);
	}
	
	public List<StackResource> getStackResources(String stackName) {
		if (getProject().getGradle().getStartParameter().isOffline() == false) {
			try {
				DescribeStackResourcesResult describeStackResourcesResult =
						getClient().describeStackResources(new DescribeStackResourcesRequest()
							.withStackName(stackName));
				return describeStackResourcesResult.getStackResources();
			} catch (AmazonClientException e) {
				logger.error("describeStackResources failed: {}", e.getMessage());
			}
		}
		logger.info("offline mode: return empty resources");
		return Collections.emptyList();
	}
	
	public String getStackParameterValue(String key) {
		return findStackParameterValue(getStackParameters(), key);
	}
	
	public String getStackParameterValue(String stackName, String key) {
		return findStackParameterValue(getStackParameters(stackName), key);
	}
	
	public String findStackParameterValue(List<Parameter> cfnStackParameters, String key) {
		Optional<Parameter> param = cfnStackParameters.stream()
				.filter(p -> p.getParameterKey().equals(key))
				.findAny();
		if (param.isPresent() == false) {
			logger.warn("WARN: cfn stack parameter {} is not found", key);
			return "***unknown***";
		}
		return param.get().getParameterValue();
	}

	public String getPhysicalResourceId(String logicalResourceId) {
		return findPhysicalResourceId(getStackResources(), logicalResourceId);
	}

	public String getPhysicalResourceId(String stackName, String logicalResourceId) {
		return findPhysicalResourceId(getStackResources(stackName), logicalResourceId);
	}
	
	public String findPhysicalResourceId(List<StackResource> cfnPhysicalResources, String logicalResourceId) {
		Optional<StackResource> cfnPhysicalResource = cfnPhysicalResources.stream()
				.filter(r -> r.getLogicalResourceId().equals(logicalResourceId)).findAny();
		if (cfnPhysicalResource.isPresent() == false) {
			logger.warn("WARN: cfn physical resource {} is not found", logicalResourceId);
			return "***unknown***";
		}
		return cfnPhysicalResource.get().getPhysicalResourceId();
	}
	
	public List<Parameter> toParameters(Map<String, String> map) {
		return map.entrySet().stream()
				.map(e -> new Parameter().withParameterKey(e.getKey()).withParameterValue(e.getValue()))
				.collect(Collectors.toList());
	}
	
	public Map<String, String> toMap(List<Parameter> parameters) {
		return parameters.stream().collect(Collectors.toMap(Parameter::getParameterKey, Parameter::getParameterValue));
	}
}
