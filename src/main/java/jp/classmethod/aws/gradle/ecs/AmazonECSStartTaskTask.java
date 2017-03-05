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
import com.amazonaws.services.ecs.model.StartTaskRequest;
import com.amazonaws.services.ecs.model.StartTaskResult;

public class AmazonECSStartTaskTask extends ConventionTask {
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String taskDefinition;
	
	@Getter
	@Setter
	private List<String> containerInstances;
	
	
	public void containerInstances(List<String> containerInstances) {
		this.containerInstances = containerInstances;
	}
	
	public void containerInstances(String... containerInstances) {
		this.containerInstances = Arrays.asList(containerInstances);
	}
	
	
	@Getter
	@Setter
	private String startedBy;
	
	@Getter
	private StartTaskResult startTaskResult;
	
	
	public AmazonECSStartTaskTask() {
		setDescription("Start Task Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void startTask() {
		// to enable conventionMappings feature
		String cluster = getCluster();
		String taskDefinition = getTaskDefinition();
		List<String> containerInstances = getContainerInstances();
		String startedBy = getStartedBy();
		
		if (cluster == null) {
			throw new GradleException("Cluster is required");
		}
		
		if (taskDefinition == null) {
			throw new GradleException("Task Definition is required");
		}
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		StartTaskRequest request = new StartTaskRequest()
			.withCluster(cluster)
			.withTaskDefinition(taskDefinition)
			.withContainerInstances(containerInstances)
			.withStartedBy(startedBy);
		
		startTaskResult = ecs.startTask(request);
		
		String taskArns = startTaskResult.getTasks().stream()
			.map(i -> i.getTaskArn())
			.collect(Collectors.joining(", "));
		getLogger().info("Start ECS task task requested: {}", taskArns);
	}
}
