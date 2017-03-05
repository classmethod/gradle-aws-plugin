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
package jp.classmethod.aws.gradle.ecs;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.DeploymentConfiguration;
import com.amazonaws.services.ecs.model.UpdateServiceRequest;
import com.amazonaws.services.ecs.model.UpdateServiceResult;
import com.amazonaws.services.ecs.model.transform.DeploymentConfigurationJsonUnmarshaller;

public class AmazonECSUpdateServiceTask extends ConventionTask {
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String service;
	
	@Getter
	@Setter
	private Integer desiredCount;
	
	@Getter
	@Setter
	private String taskDefinition;
	
	@Getter
	@Setter
	private String deploymentConfigurationJson;
	
	@Getter
	@Setter
	private DeploymentConfiguration deploymentConfiguration;
	
	@Getter
	private UpdateServiceResult updateServiceResult;
	
	
	public AmazonECSUpdateServiceTask() {
		setDescription("Update Service Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void updateService() {
		// to enable conventionMappings feature
		deploymentConfiguration = JsonUnmarshallerContextHelper.parseObject(
				DeploymentConfigurationJsonUnmarshaller.getInstance(),
				"deploymentConfigurationJson", deploymentConfigurationJson);
		
		String cluster = getCluster();
		String service = getService();
		Integer desiredCount = getDesiredCount();
		String taskDefinition = getTaskDefinition();
		DeploymentConfiguration deploymentConfiguration = getDeploymentConfiguration();
		
		if (cluster == null) {
			throw new GradleException("Cluster is required");
		}
		
		if (taskDefinition == null) {
			throw new GradleException("Task Definition is required");
		}
		
		if (desiredCount == null) {
			throw new GradleException("Desired Count is required");
		}
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		UpdateServiceRequest request = new UpdateServiceRequest()
			.withCluster(cluster)
			.withService(service)
			.withDesiredCount(desiredCount)
			.withTaskDefinition(taskDefinition)
			.withDeploymentConfiguration(deploymentConfiguration);
		
		updateServiceResult = ecs.updateService(request);
		
		String serviceArn = updateServiceResult.getService().getServiceArn();
		getLogger().info("Create ECS Service task requested: {}", serviceArn);
	}
}
