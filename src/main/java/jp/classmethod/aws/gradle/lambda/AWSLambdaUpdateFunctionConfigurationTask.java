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
package jp.classmethod.aws.gradle.lambda;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.Environment;
import com.amazonaws.services.lambda.model.ListTagsRequest;
import com.amazonaws.services.lambda.model.ListTagsResult;
import com.amazonaws.services.lambda.model.TagResourceRequest;
import com.amazonaws.services.lambda.model.UntagResourceRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class AWSLambdaUpdateFunctionConfigurationTask extends ConventionTask {
	
	@Getter
	@Setter
	private String functionName;
	
	@Getter
	@Setter
	private String role;
	
	@Getter
	@Setter
	private String handler;
	
	@Getter
	@Setter
	private String functionDescription;
	
	@Getter
	@Setter
	private Integer timeout;
	
	@Getter
	@Setter
	private Integer memorySize;
	
	@Getter
	@Setter
	private Map<String, String> environment;
	
	@Getter
	@Setter
	private Map<String, String> tags;
	
	@Getter
	private UpdateFunctionConfigurationResult updateFunctionConfiguration;
	
	
	public AWSLambdaUpdateFunctionConfigurationTask() {
		setDescription("Update Lambda function configuration.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void createFunction() throws FileNotFoundException, IOException {
		// to enable conventionMappings feature
		String functionName = getFunctionName();
		
		if (functionName == null) {
			throw new GradleException("functionName is required");
		}
		
		AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
		AWSLambda lambda = ext.getClient();
		
		UpdateFunctionConfigurationRequest request = new UpdateFunctionConfigurationRequest()
			.withFunctionName(getFunctionName())
			.withRole(getRole())
			.withHandler(getHandler())
			.withDescription(getFunctionDescription())
			.withTimeout(getTimeout())
			.withMemorySize(getMemorySize())
			.withEnvironment(new Environment().withVariables(getEnvironment()));
		updateFunctionConfiguration = lambda.updateFunctionConfiguration(request);
		getLogger().info("Update Lambda function configuration requested: {}",
				updateFunctionConfiguration.getFunctionArn());
		
		tagFunction(lambda);
	}
	
	private void tagFunction(AWSLambda lambda) {
		if (getTags() != null) {
			ListTagsRequest listTagsRequest = new ListTagsRequest()
				.withResource(getUpdateFunctionConfiguration().getFunctionArn());
			
			ListTagsResult listTagsResult = lambda.listTags(listTagsRequest);
			
			if (!listTagsResult.getTags().isEmpty()) {
				MapDifference<String, String> tagDifferences =
						Maps.difference(listTagsResult.getTags(), getTags());
				
				UntagResourceRequest untagResourceRequest = new UntagResourceRequest()
					.withResource(getUpdateFunctionConfiguration().getFunctionArn())
					.withTagKeys(tagDifferences.entriesOnlyOnLeft().keySet());
				lambda.untagResource(untagResourceRequest);
			}
			
			if (!getTags().isEmpty()) {
				TagResourceRequest tagResourceRequest = new TagResourceRequest()
					.withTags(getTags())
					.withResource(getUpdateFunctionConfiguration().getFunctionArn());
				
				lambda.tagResource(tagResourceRequest);
				getLogger().info("Update Lambda function tags requested: {}",
						getUpdateFunctionConfiguration().getFunctionArn());
			}
		}
	}
}
