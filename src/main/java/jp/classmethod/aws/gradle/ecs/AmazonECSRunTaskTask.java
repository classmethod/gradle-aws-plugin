/*
 * Copyright 2013-2016 Classmethod, Inc.
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

import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.RunTaskRequest;
import com.amazonaws.services.ecs.model.RunTaskResult;

public class AmazonECSRunTaskTask extends ConventionTask {
	
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String taskDefinition;
	
	@Getter
	@Setter
	private int count;
	
	@Getter
	@Setter
	private String startedBy;
	
	@Getter
	private RunTaskResult runTaskResult;
	
	
	public AmazonECSRunTaskTask() {
		setDescription("Run ECS Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void runTask() {
		// to enable conventionMappings feature
		String cluster = getCluster();
		String taskDefinition = getTaskDefinition();
		int count = getCount();
		String startedBy = getStartedBy();
		
		if (taskDefinition == null)
			throw new GradleException("Task Definition ARN is required");
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		RunTaskRequest request = new RunTaskRequest()
			.withCluster(cluster)
			.withTaskDefinition(taskDefinition)
			.withCount(count)
			.withStartedBy(startedBy);
		
		runTaskResult = ecs.runTask(request);
		
		String taskArns = runTaskResult.getTasks().stream()
			.map(i -> i.getTaskArn())
			.collect(Collectors.joining(", "));
		getLogger().info("Run ECS task requested: {}", taskArns);
	}
}
