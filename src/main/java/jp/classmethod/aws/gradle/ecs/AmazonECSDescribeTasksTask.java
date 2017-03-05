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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.DescribeTasksRequest;
import com.amazonaws.services.ecs.model.DescribeTasksResult;

public class AmazonECSDescribeTasksTask extends ConventionTask {

	@Setter
	@Getter
	private String cluster;

	@Getter
	private List<String> tasks;


	public void tasks(List<String> tasks) {
		this.tasks = tasks;
	}

	public void tasks(String... tasks) {
		this.tasks = Arrays.asList(tasks);
	}


	@Getter
	private DescribeTasksResult describeTaskDefinitionResult;


	public AmazonECSDescribeTasksTask() {
		setDescription("Describe Tasks Task.");
		setGroup("AWS");
	}

	@TaskAction
	public void describeTasks() {
		// to enable conventionMappings feature
		String cluster = getCluster();

		List<String> tasks = getTasks();

		if (cluster == null) {
			throw new GradleException("Clusters is required");
		}

		if (tasks == null) {
			throw new GradleException("Tasks is required");
		}

		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();

		DescribeTasksRequest request = new DescribeTasksRequest()
			.withCluster(cluster)
			.withTasks(tasks);

		describeTaskDefinitionResult = ecs.describeTasks(request);

		String taskArns = describeTaskDefinitionResult.getTasks().stream()
			.map(i -> i.getTaskArn())
			.collect(Collectors.joining(", "));
		getLogger().info("Describe ECS Tasks task requested: {}", taskArns);
	}
}
