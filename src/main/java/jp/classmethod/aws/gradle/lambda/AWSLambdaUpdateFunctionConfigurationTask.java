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
package jp.classmethod.aws.gradle.lambda;

import java.io.FileNotFoundException;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;

public class AWSLambdaUpdateFunctionConfigurationTask extends ConventionTask {
	
	@Getter @Setter
	private String functionName;
	
	@Getter @Setter
	private String role;
	
	@Getter @Setter
	private String handler;
	
	@Getter @Setter
	private String functionDescription;
	
	@Getter @Setter
	private Integer timeout;
	
	@Getter @Setter
	private Integer memorySize;
	
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
		
		if (functionName == null) throw new GradleException("functionName is required");
		
		AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
		AWSLambda lambda = ext.getClient();
		
		UpdateFunctionConfigurationRequest request = new UpdateFunctionConfigurationRequest()
			.withFunctionName(getFunctionName())
			.withRole(getRole())
			.withHandler(getHandler())
			.withDescription(getFunctionDescription())
			.withTimeout(getTimeout())
			.withMemorySize(getMemorySize());
		updateFunctionConfiguration = lambda.updateFunctionConfiguration(request);
		getLogger().info("Update Lambda function configuration requested: {}", updateFunctionConfiguration.getFunctionArn());
	}
}
