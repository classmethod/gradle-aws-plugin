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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.SubmitTaskStateChangeRequest;
import com.amazonaws.services.ecs.model.SubmitTaskStateChangeResult;

public class AmazonECSSubmitTaskStateChangeTask extends ConventionTask {
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String task;
	
	@Getter
	@Setter
	private String status;
	
	@Getter
	@Setter
	private String reason;
	
	@Getter
	private SubmitTaskStateChangeResult submitTaskStateChangeResult;
	
	
	public AmazonECSSubmitTaskStateChangeTask() {
		setDescription("Submit Task State Change Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void submitTaskStateChange() {
		// to enable conventionMappings feature
		String cluster = getCluster();
		String task = getTask();
		String status = getStatus();
		String reason = getReason();
		
		if (cluster == null) {
			throw new GradleException("Cluster is required");
		}
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		SubmitTaskStateChangeRequest request = new SubmitTaskStateChangeRequest()
			.withCluster(cluster)
			.withTask(task)
			.withStatus(status)
			.withReason(reason);
		
		submitTaskStateChangeResult = ecs.submitTaskStateChange(request);
		
		String acknowledgment = submitTaskStateChangeResult.getAcknowledgment();
		getLogger().info("Submit ECS Task State task requested: {}", acknowledgment);
	}
}
