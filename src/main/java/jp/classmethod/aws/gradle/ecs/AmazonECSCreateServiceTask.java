/*
 * Copyright 2013-2017 Classmethod, Inc.
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

// -----------------------------------------------------------------------------
// Tasks related to Amazon EC2 Container Service.
//
// @author Dongjun Lee (chaz.epps@gmail.com)
// -----------------------------------------------------------------------------

package jp.classmethod.aws.gradle.ecs;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.CreateServiceRequest;
import com.amazonaws.services.ecs.model.CreateServiceResult;
import com.amazonaws.services.ecs.model.DeploymentConfiguration;
import com.amazonaws.services.ecs.model.LoadBalancer;
import com.amazonaws.services.ecs.model.transform.LoadBalancerJsonUnmarshaller;

public class AmazonECSCreateServiceTask extends ConventionTask {

	@Getter
	@Setter
	private String cluster;

	@Getter
	@Setter
	private String serviceName;

	@Getter
	@Setter
	private String taskDefinition;

	@Getter
	@Setter
	private String loadBalancersJson;

	@Getter
	@Setter
	private List<LoadBalancer> loadBalancers;

	@Getter
	@Setter
	private Integer desiredCount;

	@Getter
	@Setter
	private String clientToken;

	@Getter
	@Setter
	private String role;

	@Getter
	@Setter
	private String deploymentConfigurationJson;

	@Getter
	@Setter
	private DeploymentConfiguration deploymentConfiguration;

	@Getter
	private CreateServiceResult createServiceResult;


	public AmazonECSCreateServiceTask() {
		setDescription("Create Service Task.");
		setGroup("AWS");
	}

	@TaskAction
	public void createService() {
		// to enable conventionMappings feature
		loadBalancers = JsonUnmarshallerContextHelper.parse(
				LoadBalancerJsonUnmarshaller.getInstance(), "loadBalancersJson",
				loadBalancersJson);

		String cluster = getCluster();
		String serviceName = getServiceName();
		String taskDefinition = getTaskDefinition();
		List<LoadBalancer> loadBalancers = getLoadBalancers();
		Integer desiredCount = getDesiredCount();
		String clientToken = getClientToken();
		String role = getRole();
		DeploymentConfiguration deploymentConfiguration = getDeploymentConfiguration();

		if (cluster == null) {
			throw new GradleException("Cluster is required");
		}

		if (serviceName == null) {
			throw new GradleException("Service Name is required");
		}

		if (taskDefinition == null) {
			throw new GradleException("Task Definition is required");
		}

		if (desiredCount == null) {
			throw new GradleException("Desired Count is required");
		}

		AmazonECSPluginExtension ext = getProject().getExtensions()
			.getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();

		CreateServiceRequest request = new CreateServiceRequest()
			.withCluster(cluster)
			.withServiceName(serviceName)
			.withTaskDefinition(taskDefinition)
			.withLoadBalancers(loadBalancers)
			.withDesiredCount(desiredCount)
			.withClientToken(clientToken)
			.withRole(role)
			.withDeploymentConfiguration(deploymentConfiguration);

		createServiceResult = ecs.createService(request);

		String serviceArn = createServiceResult.getService().getServiceArn();
		getLogger().info("Create ECS Service task requested: {}", serviceArn);
	}
}
