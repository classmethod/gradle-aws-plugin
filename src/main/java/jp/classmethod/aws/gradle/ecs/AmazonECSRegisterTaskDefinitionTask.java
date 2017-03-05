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

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionResult;
import com.amazonaws.services.ecs.model.Volume;
import com.amazonaws.services.ecs.model.transform.ContainerDefinitionJsonUnmarshaller;
import com.amazonaws.services.ecs.model.transform.VolumeJsonUnmarshaller;

public class AmazonECSRegisterTaskDefinitionTask extends ConventionTask {

	@Getter
	@Setter
	private String family;

	@Getter
	@Setter
	private String taskRoleArn;

	@Getter
	@Setter
	private String networkMode;

	@Getter
	@Setter
	private String containerDefinitionsJson;

	@Getter
	private List<ContainerDefinition> containerDefinitions;

	@Getter
	@Setter
	private String volumesJson;

	@Getter
	private List<Volume> volumes;

	@Getter
	private RegisterTaskDefinitionResult registerTaskDefinitionResult;


	public AmazonECSRegisterTaskDefinitionTask() {
		setDescription("Register Task Definition Task.");
		setGroup("AWS");
	}

	@TaskAction
	public void registerTaskDefinition() {
		// to enable conventionMappings feature
		containerDefinitions = JsonUnmarshallerContextHelper.parse(
				ContainerDefinitionJsonUnmarshaller.getInstance(), "containerDefinitionsJson",
				containerDefinitionsJson);

		if (volumesJson != null) {
			volumes = JsonUnmarshallerContextHelper.parse(
					VolumeJsonUnmarshaller.getInstance(), "volumesJson",
					volumesJson);
		}

		String family = getFamily();
		String taskRoleArn = getTaskRoleArn();
		String networkMode = getNetworkMode();
		List<ContainerDefinition> containerDefinitions = getContainerDefinitions();
		List<Volume> volumes = getVolumes();

		if (family == null) {
			throw new GradleException("Family is required");
		}

		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();

		RegisterTaskDefinitionRequest request = new RegisterTaskDefinitionRequest()
			.withFamily(family)
			.withTaskRoleArn(taskRoleArn)
			.withNetworkMode(networkMode)
			.withContainerDefinitions(containerDefinitions)
			.withVolumes(volumes);

		registerTaskDefinitionResult = ecs.registerTaskDefinition(request);
		String taskDefinitionArn = registerTaskDefinitionResult.getTaskDefinition().getTaskDefinitionArn();
		getLogger().info("Register Task Deninnition: {}", taskDefinitionArn);
	}
}
