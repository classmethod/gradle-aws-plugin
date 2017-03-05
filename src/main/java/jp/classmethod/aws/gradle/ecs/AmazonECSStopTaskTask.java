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
import com.amazonaws.services.ecs.model.StopTaskRequest;
import com.amazonaws.services.ecs.model.StopTaskResult;

public class AmazonECSStopTaskTask extends ConventionTask {

	@Getter
	@Setter
	private String cluster;

	@Getter
	@Setter
	private String task;

	@Getter
	@Setter
	private String reason;

	@Getter
	private StopTaskResult stopTaskResult;


	public AmazonECSStopTaskTask() {
		setDescription("Stop Task Task.");
		setGroup("AWS");
	}

	@TaskAction
	public void stopTask() {
		// to enable conventionMappings feature
		String cluster = getCluster();
		String task = getTask();
		String reason = getReason();

		if (cluster == null) {
			throw new GradleException("Cluster is required");
		}

		if (task == null) {
			throw new GradleException("Task is required");
		}

		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();

		StopTaskRequest request = new StopTaskRequest()
			.withCluster(cluster)
			.withTask(task)
			.withReason(reason);

		stopTaskResult = ecs.stopTask(request);

		String taskArn = stopTaskResult.getTask().getTaskArn();
		getLogger().info("Stop ECS Task task requested: {}", taskArn);
	}
}
