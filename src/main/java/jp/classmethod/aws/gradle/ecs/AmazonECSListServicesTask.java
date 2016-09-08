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
import com.amazonaws.services.ecs.model.ListServicesRequest;
import com.amazonaws.services.ecs.model.ListServicesResult;

public class AmazonECSListServicesTask extends ConventionTask {
	
	
	@Setter
	@Getter
	private String cluster;
	
	@Getter
	private ListServicesResult listServicesResult;
	
	
	public AmazonECSListServicesTask() {
		setDescription("List Services Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void listServices() {
		// to enable conventionMappings feature
		String clusters = getCluster();
		
		if (clusters == null)
			throw new GradleException("Clusters is required");
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		ListServicesRequest request = new ListServicesRequest()
			.withCluster(cluster);
		
		listServicesResult = ecs.listServices(request);
		
		String serviceArns = listServicesResult.getServiceArns().stream()
			.collect(Collectors.joining(", "));
		getLogger().info("List ECS Services task requested: {}", serviceArns);
	}
}
