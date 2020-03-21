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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.logging.Logger;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AddPermissionRequest;
import com.amazonaws.services.lambda.model.AddPermissionResult;
import com.amazonaws.services.lambda.model.RemovePermissionRequest;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;

public class ResourcePermission {
	
	@Getter
	@Setter
	private String action;
	
	@Getter
	@Setter
	private String principal;
	
	@Getter
	@Setter
	private String sourceArn;
	
	@Getter
	@Setter
	private String statementId;
	
	
	protected ResourcePermission() {
		/*
		An empty constructor is needed so that gradle can resolve variable to an instance,
		eg to make this work as a nested task property
		 */
	}
	
	static ResourcePermission of(String statementId, String action, String principal, String sourceArn) {
		final ResourcePermission resourcePermission = new ResourcePermission();
		resourcePermission.setStatementId(statementId);
		resourcePermission.setAction(action);
		resourcePermission.setPrincipal(principal);
		resourcePermission.setSourceArn(sourceArn);
		return resourcePermission;
	}
	
	static void createOrUpdateResourcePermissions(final AWSLambda lambda, final Logger logger,
			final String functionName, final Collection<ResourcePermission> resourcePermissions) {
		final List<AddPermissionResult> functionResourcePolicyResults =
				resourcePermissions.stream().map(rp -> rp.addOrUpdatePermissionToPolicy(lambda, functionName))
					.collect(Collectors.toList());
		functionResourcePolicyResults
			.forEach(result -> logger.info("Create Lambda function resource policy statement requested: {}", result));
	}
	
	public AddPermissionResult addOrUpdatePermissionToPolicy(final AWSLambda lambda, final String functionName) {
		final RemovePermissionRequest removePermissionRequest = new RemovePermissionRequest();
		removePermissionRequest.setStatementId(statementId);
		removePermissionRequest.setFunctionName(functionName);
		try {
			lambda.removePermission(removePermissionRequest);
		} catch (ResourceNotFoundException e) {
			// nothing to do here, we will create the resource next
		}
		
		final AddPermissionRequest addPermissionRequest = new AddPermissionRequest();
		addPermissionRequest.setStatementId(statementId);
		addPermissionRequest.setFunctionName(functionName);
		addPermissionRequest.setAction(this.getAction());
		addPermissionRequest.setPrincipal(this.getPrincipal());
		addPermissionRequest.setSourceArn(this.getSourceArn());
		return lambda.addPermission(addPermissionRequest);
	}
}
