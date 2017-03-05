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

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.ListClustersRequest;
import com.amazonaws.services.ecs.model.ListClustersResult;

public class AmazonECSListClustersTask extends ConventionTask {
	
	
	@Getter
	private ListClustersResult listClustersResult;
	
	
	public AmazonECSListClustersTask() {
		setDescription("List Clusters Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void listClusters() {
		// to enable conventionMappings feature
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		ListClustersRequest request = new ListClustersRequest();
		
		listClustersResult = ecs.listClusters(request);
		
		String clusterArns = listClustersResult.getClusterArns().stream()
			.collect(Collectors.joining(", "));
		getLogger().info("List ECS Clusters task requested: {}", clusterArns);
	}
}
