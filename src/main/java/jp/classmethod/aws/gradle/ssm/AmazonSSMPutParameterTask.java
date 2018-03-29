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
package jp.classmethod.aws.gradle.ssm;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterAlreadyExistsException;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonSSMPutParameterTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private List<Parameter> parameters = new ArrayList<>();
	
	/**
	 * If overwrite is set true, the parameter which has the same
	 * name will be overwritten with the task execution.
	 * The default value is false.
	 */
	@Getter
	@Setter
	private boolean overwrite;
	
	/**
	 * used to group parameters with specific prefix.
	 * when prefix = "foo." and parameter name = "bar",
	 * actual parameter name will be "foo.bar".
	 */
	@Getter
	@Setter
	private String prefix;
	
	@Getter
	private PutParameterResult putParameterResult;
	
	
	public AmazonSSMPutParameterTask() {
		super("AWS", "Put SSM Parameters.");
	}
	
	@TaskAction
	public void putParameter() {
		// to enable conventionMappings feature
		List<Parameter> parameters = getParameters();
		
		if (parameters.isEmpty()) {
			return;
		}
		
		if (getPrefix() == null) {
			setPrefix("");
		}
		
		AmazonSSMPluginExtention ext = getPluginExtension(AmazonSSMPluginExtention.class);
		AWSSimpleSystemsManagementClient ssm = ext.getClient();
		
		parameters.stream()
			.map(param -> new PutParameterRequest()
				.withName(getPrefix() + param.getName())
				.withType(param.getType())
				.withValue(param.getValue())
				.withOverwrite(isOverwrite()))
			.forEach(request -> {
				try {
					ssm.putParameter(request);
				} catch (ParameterAlreadyExistsException e) {
					getLogger().warn("parameter {} already exists", request.getName());
				}
			});
		
		getLogger().info("Successfully Put SSM Parameters.");
	}
}
