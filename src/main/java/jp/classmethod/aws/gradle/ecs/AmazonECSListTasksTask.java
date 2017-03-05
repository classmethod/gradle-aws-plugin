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
import com.amazonaws.services.ecs.model.ListTasksRequest;
import com.amazonaws.services.ecs.model.ListTasksResult;

public class AmazonECSListTasksTask extends ConventionTask {
	
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String containerInstance;
	
	@Getter
	@Setter
	private String family;
	
	@Getter
	@Setter
	private String startedBy;
	
	@Getter
	@Setter
	private String serviceName;
	
	@Getter
	@Setter
	private String desiredStatus;
	
	@Getter
	private ListTasksResult listTasksResult;
	
	
	public AmazonECSListTasksTask() {
		setDescription("List Tasks Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void deleteCluster() {
		// to enable conventionMappings feature
		
		String cluster = getCluster();
		String containerInstance = getContainerInstance();
		String family = getFamily();
		String startedBy = getStartedBy();
		String serviceName = getServiceName();
		String desiredStatus = getDesiredStatus();
		
		String clusters = getCluster();
		
		if (clusters == null)
			throw new GradleException("Clusters is required");
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		ListTasksRequest request = new ListTasksRequest()
			.withCluster(cluster)
			.withContainerInstance(containerInstance)
			.withFamily(family)
			.withStartedBy(startedBy)
			.withServiceName(serviceName)
			.withDesiredStatus(desiredStatus);
		
		listTasksResult = ecs.listTasks(request);
		
		String taskArns = listTasksResult.getTaskArns().stream()
			.collect(Collectors.joining(", "));
		getLogger().info("List ECS Tasks task requested: {}", taskArns);
	}
}
