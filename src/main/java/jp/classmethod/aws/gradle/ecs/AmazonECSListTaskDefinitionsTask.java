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

import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.ListTaskDefinitionsRequest;
import com.amazonaws.services.ecs.model.ListTaskDefinitionsResult;

public class AmazonECSListTaskDefinitionsTask extends ConventionTask {
	
	@Setter
	@Getter
	private String familyPrefix;
	
	@Setter
	@Getter
	private String status;
	
	@Getter
	private ListTaskDefinitionsResult listTaskDefinitionsResult;
	
	
	public AmazonECSListTaskDefinitionsTask() {
		setDescription("List Task Definitions Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void listTaskDefinitions() {
		// to enable conventionMappings feature
		String familyPrefix = getFamilyPrefix();
		String status = getStatus();
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		ListTaskDefinitionsRequest request = new ListTaskDefinitionsRequest()
			.withFamilyPrefix(familyPrefix)
			.withStatus(status);
		
		listTaskDefinitionsResult = ecs.listTaskDefinitions(request);
		
		String serviceArns = listTaskDefinitionsResult.getTaskDefinitionArns().stream()
			.collect(Collectors.joining(", "));
		getLogger().info("List ECS Task Definitions task requested: {}", serviceArns);
	}
}
