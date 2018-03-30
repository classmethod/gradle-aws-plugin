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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;

import jp.classmethod.aws.gradle.common.BasePluginExtension;

@Slf4j
public class AmazonCloudFormationPluginExtension extends BasePluginExtension<AmazonCloudFormation> {
	
	public static final String NAME = "cloudFormation";
	
	@Getter
	@Setter
	private String stackName;
	
	@Getter
	@Setter
	private Map<?, ?> stackParams = new HashMap<>();
	
	@Getter
	@Setter
	private Map<?, ?> stackTags = new HashMap<>();
	
	@Getter
	@Setter
	private String templateURL;
	
	@Getter
	@Setter
	private String onFailure;
	
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
	
	@Getter
	@Setter
	private Capability useCapabilityIam;
	
	
	public AmazonCloudFormationPluginExtension(Project project) {
		super(project, AmazonCloudFormationClientBuilder.standard());
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
			} catch (AmazonCloudFormationException e) {
				if ("ValidationError".equals(e.getErrorCode())) {
					return Optional.empty();
				} else {
					throw new GradleException(e.getMessage(), e);
				}
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
		log.info("offline mode: return empty parameters");
		return Collections.emptyList();
	}
	
	public List<Output> getStackOutputs() {
		return getStackOutputs(stackName);
	}
	
	public List<Output> getStackOutputs(String stackName) {
		if (getProject().getGradle().getStartParameter().isOffline() == false) {
			Optional<Stack> stack = getStack(stackName);
			return stack.map(Stack::getOutputs).orElse(Collections.emptyList());
		}
		log.info("offline mode: return empty outputs");
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
			} catch (AmazonCloudFormationException e) {
				if ("ValidationError".equals(e.getErrorCode())) {
					return Collections.emptyList();
				} else {
					throw new GradleException(e.getMessage(), e);
				}
			}
		}
		log.info("offline mode: return empty resources");
		return Collections.emptyList();
	}
	
	public String getStackParameterValue(String key) {
		return getStackParameterValue(stackName, key);
	}
	
	public String getStackParameterValue(String stackName, String key) {
		return findStackParameterValue(stackName, key);
	}
	
	public String findStackParameterValue(String stackName, String key) {
		Optional<Parameter> param = getStackParameters(stackName).stream()
			.filter(p -> p.getParameterKey().equals(key))
			.findAny();
		if (param.isPresent() == false) {
			log.warn("WARN: param {} for stack {} is not found", key, stackName);
		}
		return param.map(Parameter::getParameterValue).orElse(null);
	}
	
	public String getStackOutputValue(String key) {
		return getStackOutputValue(stackName, key);
	}
	
	public String getStackOutputValue(String stackName, String key) {
		return findStackOutputValue(stackName, key);
	}
	
	public String findStackOutputValue(String stackName, String key) {
		Optional<Output> output = getStackOutputs(stackName).stream()
			.filter(p -> p.getOutputKey().equals(key))
			.findAny();
		if (output.isPresent() == false) {
			log.warn("WARN: output {} for stack {} is not found", key, stackName);
		}
		return output.map(Output::getOutputValue).orElse(null);
	}
	
	public String getPhysicalResourceId(String logicalResourceId) {
		return getPhysicalResourceId(stackName, logicalResourceId);
	}
	
	public String getPhysicalResourceId(String stackName, String logicalResourceId) {
		return findPhysicalResourceId(stackName, logicalResourceId);
	}
	
	public String findPhysicalResourceId(String stackName, String logicalResourceId) {
		Optional<StackResource> physicalResource = getStackResources(stackName).stream()
			.filter(r -> r.getLogicalResourceId().equals(logicalResourceId))
			.findAny();
		if (physicalResource.isPresent() == false) {
			log.warn("WARN: physical resource {} for stack {} is not found", logicalResourceId, stackName);
		}
		return physicalResource.map(StackResource::getPhysicalResourceId).orElse(null);
	}
	
	public boolean isValidTemplateBody(String templateBody) {
		try {
			ValidateTemplateRequest validateTemplateRequest =
					new ValidateTemplateRequest().withTemplateBody(templateBody);
			getClient().validateTemplate(validateTemplateRequest);
			return true;
		} catch (AmazonClientException e) {
			log.error("validateTemplateBody failed: {}", e.getMessage());
			return false;
		}
	}
	
	public boolean isValidTemplateUrl(String templateUrl) {
		try {
			ValidateTemplateRequest validateTemplateRequest =
					new ValidateTemplateRequest().withTemplateURL(templateUrl);
			getClient().validateTemplate(validateTemplateRequest);
			return true;
		} catch (AmazonClientException e) {
			log.error("validateTemplateUrl failed: {}", e.getMessage());
			return false;
		}
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
