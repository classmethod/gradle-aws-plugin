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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.DescribeContainerInstancesRequest;
import com.amazonaws.services.ecs.model.DescribeContainerInstancesResult;

public class AmazonECSDescribeContainerInstancesTask extends ConventionTask {
	
	@Getter
	private List<String> containerInstance;
	
	
	public void containerInstance(List<String> containerInstance) {
		this.containerInstance = containerInstance;
	}
	
	public void containerInstance(String... containerInstance) {
		this.containerInstance = Arrays.asList(containerInstance);
	}
	
	
	@Setter
	@Getter
	private String cluster;
	
	@Getter
	private DescribeContainerInstancesResult describeContainerInstancesResult;
	
	
	public AmazonECSDescribeContainerInstancesTask() {
		setDescription("Describe Container Instance Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void describeContainerInstances() {
		// to enable conventionMappings feature
		String clusters = getCluster();
		List<String> containerInstance = getContainerInstance();
		
		if (clusters == null) {
			throw new GradleException("Clusters is required");
		}
		
		if (containerInstance == null) {
			throw new GradleException("Container Instance is required");
		}
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		DescribeContainerInstancesRequest request = new DescribeContainerInstancesRequest()
			.withCluster(cluster)
			.withContainerInstances(containerInstance);
		
		describeContainerInstancesResult = ecs.describeContainerInstances(request);
		
		String containerInstanceArns = describeContainerInstancesResult.getContainerInstances().stream()
			.map(i -> i.getContainerInstanceArn())
			.collect(Collectors.joining(", "));
		getLogger().info("Describe ECS Container Instance task requested: {}", containerInstanceArns);
	}
}
