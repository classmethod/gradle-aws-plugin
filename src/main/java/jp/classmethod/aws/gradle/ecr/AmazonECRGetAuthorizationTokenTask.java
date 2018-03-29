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
package jp.classmethod.aws.gradle.ecr;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonECRGetAuthorizationTokenTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private List<String> repositoryIds;
	
	@Getter
	private List<AuthorizationData> authorizationData;
	
	
	public AmazonECRGetAuthorizationTokenTask() {
		super("AWS", "Get authorization token for ECR repository");
	}
	
	@TaskAction
	public void createRepository() {
		List<String> repositoryIds = getRepositoryIds();
		
		if (repositoryIds == null || repositoryIds.isEmpty()) {
			throw new GradleException("Must specify ECR repositoryIds");
		}
		
		AmazonECRPluginExtension ext = getPluginExtension(AmazonECRPluginExtension.class);
		AmazonECR ecr = ext.getClient();
		
		GetAuthorizationTokenResult result = ecr.getAuthorizationToken(new GetAuthorizationTokenRequest()
			.withRegistryIds(repositoryIds));
		authorizationData = result.getAuthorizationData();
	}
}
