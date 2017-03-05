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

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.DescribeClustersRequest;
import com.amazonaws.services.ecs.model.DescribeClustersResult;

public class AmazonECSDescribeClustersTask extends ConventionTask {
	
	@Getter
	private List<String> clusters;
	
	@Getter
	private DescribeClustersResult describeClustersResult;
	
	
	public void clusters(List<String> clusters) {
		this.clusters = clusters;
	}
	
	public void clusters(String... clusters) {
		this.clusters = Arrays.asList(clusters);
	}
	
	public AmazonECSDescribeClustersTask() {
		setDescription("Describe Clusters Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void describeClusters() {
		// to enable conventionMappings feature
		List<String> clusters = getClusters();
		
		if (clusters == null) {
			throw new GradleException("Clusters is required");
		}
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		DescribeClustersRequest request = new DescribeClustersRequest()
			.withClusters(clusters);
		
		describeClustersResult = ecs.describeClusters(request);
		
		String clusterArns = describeClustersResult.getClusters().stream()
			.map(i -> i.getClusterArn())
			.collect(Collectors.joining(", "));
		getLogger().info("Describe ECS Clusters task requested: {}", clusterArns);
	}
}
