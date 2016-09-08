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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.DescribeServicesRequest;
import com.amazonaws.services.ecs.model.DescribeServicesResult;

public class AmazonECSDescribeServicesTask extends ConventionTask {
	
	
	@Getter
	private List<String> service;
	
	
	public void service(ArrayList<String> service) {
		this.service = service;
	}
	
	public void service(List<String> service) {
		this.service = service;
	}
	
	public void service(String... service) {
		this.service = Arrays.asList(service);
	}
	
	
	@Setter
	@Getter
	private String cluster;
	
	@Getter
	private DescribeServicesResult describeServicesResult;
	
	
	public AmazonECSDescribeServicesTask() {
		setDescription("Describe Service Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void describeServicesTask() {
		// to enable conventionMappings feature
		String clusters = getCluster();
		List<String> service = getService();
		
		if (clusters == null)
			throw new GradleException("Clusters is required");
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		DescribeServicesRequest request = new DescribeServicesRequest()
			.withCluster(cluster)
			.withServices(service);
		
		describeServicesResult = ecs.describeServices(request);
		
		String serviceArns = describeServicesResult.getServices().stream()
			.map(i -> i.getServiceArn())
			.collect(Collectors.joining(", "));
		getLogger().info("Describe ECS Service task requested: {}", serviceArns);
	}
}
