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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.model.CreateRepositoryRequest;
import com.amazonaws.services.ecr.model.CreateRepositoryResult;
import com.amazonaws.services.ecr.model.DescribeRepositoriesRequest;
import com.amazonaws.services.ecr.model.DescribeRepositoriesResult;
import com.amazonaws.services.ecr.model.Repository;
import com.amazonaws.services.ecr.model.RepositoryAlreadyExistsException;
import com.google.common.base.MoreObjects;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonECRCreateRepositoryTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String repositoryName;
	
	@Getter
	private Repository repository;
	
	
	public AmazonECRCreateRepositoryTask() {
		super("AWS", "Create ECR repository");
	}
	
	@TaskAction
	public void createRepository() {
		AmazonECRPluginExtension ext = getPluginExtension(AmazonECRPluginExtension.class);
		AmazonECR ecr = ext.getClient();
		
		String repositoryName = MoreObjects.firstNonNull(getRepositoryName(), ext.getRepositoryName());
		
		try {
			CreateRepositoryResult result = ecr.createRepository(new CreateRepositoryRequest()
				.withRepositoryName(repositoryName));
			repository = result.getRepository();
		} catch (RepositoryAlreadyExistsException ex) {
			DescribeRepositoriesResult describeRepositoriesResult =
					ecr.describeRepositories(new DescribeRepositoriesRequest());
			for (Repository repositoryResult : describeRepositoriesResult.getRepositories()) {
				if (repositoryResult.getRepositoryName().equals(repositoryName)) {
					repository = repositoryResult;
					break;
				}
			}
		}
	}
}
