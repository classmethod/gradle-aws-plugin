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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.DescribeTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.DescribeTaskDefinitionResult;

public class AmazonECSDescribeTaskDefinitionTask extends ConventionTask {

	@Setter
	@Getter
	private String taskDefinition;

	@Getter
	private DescribeTaskDefinitionResult describeTaskDefinitionResult;


	public AmazonECSDescribeTaskDefinitionTask() {
		setDescription("Describe Task Definition Task.");
		setGroup("AWS");
	}

	@TaskAction
	public void describeTaskDefinition() {
		// to enable conventionMappings feature
		String clusters = getTaskDefinition();

		if (clusters == null) {
			throw new GradleException("Clusters is required");
		}

		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();

		DescribeTaskDefinitionRequest request = new DescribeTaskDefinitionRequest()
			.withTaskDefinition(taskDefinition);

		describeTaskDefinitionResult = ecs.describeTaskDefinition(request);

		String taskDefinitionArn = describeTaskDefinitionResult.getTaskDefinition()
			.getTaskDefinitionArn();
		getLogger().info("Describe ECS Task Definition task requested: {}", taskDefinitionArn);
	}
}
