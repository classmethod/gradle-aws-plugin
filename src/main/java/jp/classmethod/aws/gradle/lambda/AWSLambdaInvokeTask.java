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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.LogType;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

import groovy.lang.Closure;

public class AWSLambdaInvokeTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String functionName;
	
	@Getter
	@Setter
	private InvocationType invocationType;
	
	@Getter
	@Setter
	private LogType logType = LogType.None;
	
	@Getter
	@Setter
	private String clientContext;
	
	@Getter
	@Setter
	private String qualifier;
	
	@Getter
	@Setter
	private Object payload;
	
	@Getter
	private InvokeResult invokeResult;
	
	
	public AWSLambdaInvokeTask() {
		super("AWS", "Invoke Lambda function.");
	}
	
	@TaskAction
	public void invokeFunction() throws FileNotFoundException, IOException {
		// to enable conventionMappings feature
		String functionName = getFunctionName();
		
		if (functionName == null) {
			throw new GradleException("functionName is required");
		}
		
		AWSLambdaPluginExtension ext = getPluginExtension(AWSLambdaPluginExtension.class);
		AWSLambda lambda = ext.getClient();
		
		InvokeRequest request = new InvokeRequest()
			.withFunctionName(functionName)
			.withInvocationType(getInvocationType())
			.withLogType(getLogType())
			.withClientContext(getClientContext())
			.withQualifier(getQualifier());
		setupPayload(request);
		invokeResult = lambda.invoke(request);
		getLogger().info("Invoke Lambda function requested: {}", functionName);
	}
	
	private void setupPayload(InvokeRequest request) throws IOException {
		Object payload = getPayload();
		String str;
		if (payload instanceof ByteBuffer) {
			request.setPayload((ByteBuffer) payload);
			return;
		}
		if (payload != null) {
			if (payload instanceof File) {
				File file = (File) payload;
				str = Files.toString(file, Charsets.UTF_8);
			} else if (payload instanceof Closure) {
				Closure<?> closure = (Closure<?>) payload;
				str = closure.call().toString();
			} else if (payload instanceof String) {
				str = (String) payload;
			} else {
				str = payload.toString();
			}
			request.setPayload(str);
		}
	}
}
