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

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.Attribute;
import com.amazonaws.services.ecs.model.RegisterContainerInstanceRequest;
import com.amazonaws.services.ecs.model.RegisterContainerInstanceResult;
import com.amazonaws.services.ecs.model.Resource;
import com.amazonaws.services.ecs.model.VersionInfo;
import com.amazonaws.services.ecs.model.transform.AttributeJsonUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ResourceJsonUnmarshaller;
import com.amazonaws.services.ecs.model.transform.VersionInfoJsonUnmarshaller;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.ListUnmarshaller;

public class AmazonECSRegisterContainerInstanceTask extends ConventionTask {
	
	
	@Getter
	@Setter
	private String cluster;
	
	@Getter
	@Setter
	private String instanceIdentityDocument;
	
	@Getter
	@Setter
	private String instanceIdentityDocumentSignature;
	
	@Getter
	@Setter
	private String totalResourcesJson;
	
	@Getter
	@Setter
	private List<Resource> totalResources;
	
	@Getter
	@Setter
	private String versionInfoJson;
	
	@Getter
	@Setter
	private VersionInfo versionInfo;
	
	@Getter
	@Setter
	private String containerInstanceArn;
	
	@Getter
	@Setter
	private String attributesJson;
	
	@Getter
	@Setter
	private List<Attribute> attributes;
	
	@Getter
	private RegisterContainerInstanceResult registerContainerInstanceResult;
	
	
	public AmazonECSRegisterContainerInstanceTask() {
		setDescription("Register Container Instance Task.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void registerContainerInstance() {
		// to enable conventionMappings feature
		try {
			JsonUnmarshallerContext context = JsonUnmarshallerContextHelper.create(totalResourcesJson);
			totalResources = new ListUnmarshaller<Resource>(
					ResourceJsonUnmarshaller.getInstance()).unmarshall(context);
		} catch (Exception e) {
			throw new GradleException("Total Resources JSON is required");
		}
		
		try {
			JsonUnmarshallerContext context = JsonUnmarshallerContextHelper.create(versionInfoJson);
			versionInfo = VersionInfoJsonUnmarshaller.getInstance().unmarshall(context);
		} catch (Exception e) {
			throw new GradleException("Version Info JSON is required");
		}
		
		try {
			JsonUnmarshallerContext context = JsonUnmarshallerContextHelper.create(attributesJson);
			attributes = new ListUnmarshaller<Attribute>(
					AttributeJsonUnmarshaller.getInstance()).unmarshall(context);
		} catch (Exception e) {
			throw new GradleException("Attributes JSON is required");
		}
		
		String cluster = getCluster();
		String instanceIdentityDocument = getInstanceIdentityDocument();
		String instanceIdentityDocumentSignature = getInstanceIdentityDocumentSignature();
		List<Resource> totalResources = getTotalResources();
		VersionInfo versionInfo = getVersionInfo();
		String containerInstanceArn = getContainerInstanceArn();
		List<Attribute> attributes = getAttributes();
		
		if (cluster == null)
			throw new GradleException("Cluster is required");
		
		AmazonECSPluginExtension ext = getProject().getExtensions().getByType(AmazonECSPluginExtension.class);
		AmazonECS ecs = ext.getClient();
		
		RegisterContainerInstanceRequest request = new RegisterContainerInstanceRequest()
			.withCluster(cluster)
			.withInstanceIdentityDocument(instanceIdentityDocument)
			.withInstanceIdentityDocumentSignature(instanceIdentityDocumentSignature)
			.withTotalResources(totalResources)
			.withVersionInfo(versionInfo)
			.withContainerInstanceArn(containerInstanceArn)
			.withAttributes(attributes);
		
		registerContainerInstanceResult = ecs.registerContainerInstance(request);
		
		String containerInstance = registerContainerInstanceResult.getContainerInstance().getContainerInstanceArn();
		getLogger().info("Register ECS Container Instance task requested: {}", containerInstance);
	}
}
