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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.PublishVersionRequest;
import com.amazonaws.services.lambda.model.PublishVersionResult;

/**
 * Created by frankfarrell on 16/01/2018.
 *
 * https://docs.aws.amazon.com/cli/latest/reference/lambda/publish-version.html
 */
public class AWSLambdaPublishVersionTask extends ConventionTask {
	
	/*
	The function name for which the alias is created.
	Note that the length constraint applies only to the ARN.
	If you specify only the function name, it is limited to 64 characters in length.
	 */
	@Getter
	@Setter
	private String functionName;
	
	@Getter
	@Setter
	private String codeSha256;
	
	@Getter
	@Setter
	private String description;
	
	@Getter
	private PublishVersionResult publishVersionResult;
	
	
	@TaskAction
	public void publishVersion() {
		
		final String functionName = getFunctionName();
		
		if (functionName == null) {
			throw new GradleException("functionName is required");
		}
		
		final AWSLambda lambda = getAwsLambdaClient();
		
		PublishVersionRequest request = new PublishVersionRequest().withFunctionName(functionName);
		
		if (getCodeSha256() != null) {
			request.withCodeSha256(getCodeSha256());
		}
		if (getDescription() != null) {
			request.withDescription(getDescription());
		}
		
		publishVersionResult = lambda.publishVersion(request);
		
		getLogger().info("Publish lambda version for {} succeeded with version {}",
				functionName,
				publishVersionResult.getVersion());
	}
	
	private AWSLambda getAwsLambdaClient() {
		final AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
		return ext.getClient();
	}
}
