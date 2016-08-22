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
package jp.classmethod.aws.gradle.cloudformation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;

import jp.classmethod.aws.gradle.AwsPlugin;
import jp.classmethod.aws.gradle.s3.AmazonS3FileUploadTask;
import jp.classmethod.aws.gradle.s3.AmazonS3Plugin;

public class AmazonCloudFormationPlugin implements Plugin<Project> {
	
	
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(AwsPlugin.class);
		project.getPluginManager().apply(AmazonS3Plugin.class);
		project.getExtensions().create(AmazonCloudFormationPluginExtension.NAME,
				AmazonCloudFormationPluginExtension.class,
				project);
		applyTasks(project);
	}
	
	private void applyTasks(Project project) {
		AmazonCloudFormationPluginExtension cfnExt =
				project.getExtensions().findByType(AmazonCloudFormationPluginExtension.class);
		
		AmazonS3FileUploadTask awsCfnUploadTemplate =
				project.getTasks().create("awsCfnUploadTemplate", AmazonS3FileUploadTask.class, task -> {
					task.setDescription("Upload cfn template file to the Amazon S3 bucket.");
					task.conventionMapping("file", () -> cfnExt.getTemplateFile());
					task.conventionMapping("bucketName", () -> cfnExt.getTemplateBucket());
					task.conventionMapping("key", () -> {
						String name = cfnExt.getTemplateFile().getName();
						return createKey(name, project.getVersion(), cfnExt.getTemplateKeyPrefix());
					});
					task.doLast(t -> {
						cfnExt.setTemplateURL(((AmazonS3FileUploadTask) t).getResourceUrl());
					});
				});
		
		AmazonS3FileUploadTask awsCfnUploadPolicy =
				project.getTasks().create("awsCfnUploadStackPolicy", AmazonS3FileUploadTask.class, task -> {
					task.setDescription("Upload cfn stack policy file to the Amazon S3 bucket.");
					task.conventionMapping("file", () -> cfnExt.getStackPolicyFile());
					task.conventionMapping("bucketName", () -> cfnExt.getStackPolicyBucket());
					task.conventionMapping("key", () -> {
						String name = cfnExt.getStackPolicyFile().getName();
						return createKey(name, project.getVersion(), cfnExt.getStackPolicyKeyPrefix());
					});
					task.doLast(t -> {
						cfnExt.setStackPolicyURL(((AmazonS3FileUploadTask) t).getResourceUrl());
					});
				});
		
		AmazonCloudFormationMigrateStackTask awsCfnMigrateStack = project.getTasks()
			.create("awsCfnMigrateStack", AmazonCloudFormationMigrateStackTask.class, task -> {
				task.setDescription("Create/Migrate cfn stack.");
				task.mustRunAfter(awsCfnUploadTemplate);
				task.mustRunAfter(awsCfnUploadPolicy);
				task.conventionMapping("stackName", () -> cfnExt.getStackName());
				task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
				task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
					.map(it -> new Parameter()
						.withParameterKey(it.getKey().toString())
						.withParameterValue(it.getValue().toString()))
					.collect(Collectors.toList()));
				task.conventionMapping("cfnTemplateUrl", () -> cfnExt.getTemplateURL());
				task.conventionMapping("cfnStackPolicyUrl", () -> cfnExt.getStackPolicyURL());
				task.conventionMapping("cfnStackTags", () -> cfnExt.getStackTags().entrySet().stream()
					.map(it -> new Tag()
						.withKey(it.getKey().toString())
						.withValue(it.getValue().toString()))
					.collect(Collectors.toList()));
			});
		
		@SuppressWarnings("unused")
		AmazonCloudFormationCreateChangeSetTask awsCfnCreateChangeSet = project.getTasks()
			.create("awsCfnCreateChangeSet", AmazonCloudFormationCreateChangeSetTask.class, task -> {
				task.setDescription("Create cfn change set.");
				task.mustRunAfter(awsCfnUploadTemplate);
				task.conventionMapping("stackName", () -> cfnExt.getStackName());
				task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
				task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
					.map(it -> new Parameter()
						.withParameterKey(it.getKey().toString())
						.withParameterValue(it.getValue().toString()))
					.collect(Collectors.toList()));
				task.conventionMapping("cfnTemplateUrl", () -> cfnExt.getTemplateURL());
				task.conventionMapping("cfnStackTags", () -> cfnExt.getStackTags().entrySet().stream()
					.map(it -> new Tag()
						.withKey(it.getKey().toString())
						.withValue(it.getValue().toString()))
					.collect(Collectors.toList()));
			});
		
		project.getTasks().create("awsCfnWaitStackReady", AmazonCloudFormationWaitStackStatusTask.class, task -> {
			task.setDescription("Wait cfn stack for *_COMPLETE status.");
			task.mustRunAfter(awsCfnMigrateStack);
			task.conventionMapping("stackName", () -> cfnExt.getStackName());
		});
		
		AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackComplete =
				project.getTasks().create("awsCfnWaitStackComplete", AmazonCloudFormationWaitStackStatusTask.class,
						task -> {
							task.setDescription("Wait cfn stack for CREATE_COMPLETE or UPDATE_COMPLETE status.");
							task.mustRunAfter(awsCfnMigrateStack);
							task.setSuccessStatuses(Arrays.asList("CREATE_COMPLETE", "UPDATE_COMPLETE"));
							task.conventionMapping("stackName", () -> cfnExt.getStackName());
						});
		
		project.getTasks().create("awsCfnMigrateStackAndWaitCompleted")
			.dependsOn(awsCfnMigrateStack, awsCfnWaitStackComplete)
			.setDescription("Create/Migrate cfn stack, and wait stack for CREATE_COMPLETE or UPDATE_COMPLETE status.");
		
		AmazonCloudFormationDeleteStackTask awsCfnDeleteStack =
				project.getTasks().create("awsCfnDeleteStack", AmazonCloudFormationDeleteStackTask.class, task -> {
					task.setDescription("Delete cfn stack.");
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
				});
		
		AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackDeleted =
				project.getTasks().create("awsCfnWaitStackDeleted", AmazonCloudFormationWaitStackStatusTask.class,
						task -> {
							task.setDescription("Wait cfn stack for DELETE_COMPLETE status.");
							task.mustRunAfter(awsCfnDeleteStack);
							task.setSuccessStatuses(Arrays.asList("DELETE_COMPLETE"));
							task.conventionMapping("stackName", () -> cfnExt.getStackName());
						});
		
		project.getTasks().create("awsCfnDeleteStackAndWaitCompleted")
			.dependsOn(awsCfnDeleteStack, awsCfnWaitStackDeleted)
			.setDescription("Delete cfn stack, and wait stack for DELETE_COMPLETE status.");
	}
	
	private String createKey(String name, Object version, String prefix) {
		String path = name.substring(FilenameUtils.getPrefix(name).length());
		String baseName = FilenameUtils.getBaseName(name);
		String extension = FilenameUtils.getExtension(name);
		return String.format("%s/%s/%s-%s-%s%s", new Object[] {
			prefix,
			path,
			baseName,
			version,
			createTimestamp(),
			extension.length() > 0 ? "." + extension : ""
		});
	}
	
	private String createTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'_'HHmmss");
		sdf.setTimeZone(TimeZone.getDefault());
		String timestamp = sdf.format(new Date());
		return timestamp;
	}
}
