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

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.NetworkBinding;
import com.amazonaws.services.ecs.model.SubmitContainerStateChangeRequest;
import com.amazonaws.services.ecs.model.SubmitContainerStateChangeResult;
import com.amazonaws.services.ecs.model.transform.NetworkBindingJsonUnmarshaller;

public class AmazonECSSubmitContainerStateChangeTask extends ConventionTask {
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String task;
	
	@Getter
	@Setter
	private String containerName;
	
	@Getter
	@Setter
	private String status;
	
	@Getter
	@Setter
	private Integer exitCode;
	
	@Getter
	@Setter
	private String reason;
	
	@Getter
	@Setter
	private String networkBindingsJson;
	
	@Getter
	@Setter
	private List<NetworkBinding> networkBindings;
	
	@Getter
	private SubmitContainerStateChangeResult submitContainerStateChangeResult;
	
	
	public AmazonECSSubmitContainerStateChangeTask() {
		setDescription("Submit Container State Change Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void submitContainerStateChange() {
		// to enable conventionMappings feature
		
		networkBindings = JsonUnmarshallerContextHelper.parse(
				NetworkBindingJsonUnmarshaller.getInstance(), "networkBindingsJson",
				networkBindingsJson);
		
		String cluster = getCluster();
		String task = getTask();
		String containerName = getContainerName();
		String status = getStatus();
		Integer exitCode = getExitCode();
		String reason = getReason();
		List<NetworkBinding> networkBindings = getNetworkBindings();
		
		if (cluster == null) {
			throw new GradleException("Cluster is required");
		}
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		SubmitContainerStateChangeRequest request = new SubmitContainerStateChangeRequest()
			.withCluster(cluster)
			.withTask(task)
			.withContainerName(containerName)
			.withStatus(status)
			.withExitCode(exitCode)
			.withReason(reason)
			.withNetworkBindings(networkBindings);
		
		submitContainerStateChangeResult = ecs.submitContainerStateChange(request);
		
		String acknowledgement = submitContainerStateChangeResult.getAcknowledgment();
		getLogger().info("Submit ECS Container State Change task requested: {}", acknowledgement);
	}
}
