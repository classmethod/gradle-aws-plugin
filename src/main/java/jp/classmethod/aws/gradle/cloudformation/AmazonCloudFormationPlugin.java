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
package jp.classmethod.aws.gradle.cloudformation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
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
	
	//CHECKSTYLE:OFF
	private void applyTasks(Project project) { // NOPMD
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
				task.conventionMapping("useCapabilityIam", () -> cfnExt.getUseCapabilityIam());
				task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
					.map(it -> new Parameter()
						.withParameterKey(it.getKey().toString())
						.withParameterValue(it.getValue().toString()))
					.collect(Collectors.toList()));
				task.conventionMapping("cfnStackTags", () -> cfnExt.getStackTags().entrySet().stream()
					.map(it -> new Tag()
						.withKey(it.getKey().toString())
						.withValue(it.getValue().toString()))
					.collect(Collectors.toList()));
				task.conventionMapping("cfnRoleArn", () -> cfnExt.getCfnRoleArn());
				task.conventionMapping("cfnTemplateUrl", () -> cfnExt.getTemplateURL());
				task.conventionMapping("cfnTemplateFile", () -> cfnExt.getTemplateFile());
				task.conventionMapping("cfnStackPolicyUrl", () -> cfnExt.getStackPolicyURL());
				task.conventionMapping("cfnStackPolicyFile", () -> cfnExt.getStackPolicyFile());
				task.conventionMapping("cfnOnFailure", () -> cfnExt.getOnFailure());
			});
		
		project.getTasks()
			.create("awsCfnValidateTemplateUrl", AmazonCloudFormationValidateTemplateUrlTask.class,
					task -> {
						task.setDescription("Validate template URL.");
						task.conventionMapping("cfnTemplateUrl", () -> cfnExt.getTemplateURL());
						task.dependsOn(awsCfnUploadTemplate);
					});
		
		AmazonCloudFormationCreateChangeSetTask awsCfnCreateChangeSet = project.getTasks()
			.create("awsCfnCreateChangeSet", AmazonCloudFormationCreateChangeSetTask.class, task -> {
				task.setDescription("Create cfn change set.");
				task.mustRunAfter(awsCfnUploadTemplate);
				task.conventionMapping("stackName", () -> cfnExt.getStackName());
				task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
				task.conventionMapping("useCapabilityIam", () -> cfnExt.getUseCapabilityIam());
				task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
					.map(it -> new Parameter()
						.withParameterKey(it.getKey().toString())
						.withParameterValue(it.getValue().toString()))
					.collect(Collectors.toList()));
				task.conventionMapping("cfnStackTags", () -> cfnExt.getStackTags().entrySet().stream()
					.map(it -> new Tag()
						.withKey(it.getKey().toString())
						.withValue(it.getValue().toString()))
					.collect(Collectors.toList()));
				task.conventionMapping("cfnRoleArn", () -> cfnExt.getCfnRoleArn());
				task.conventionMapping("cfnTemplateUrl", () -> cfnExt.getTemplateURL());
				task.conventionMapping("cfnTemplateFile", () -> cfnExt.getTemplateFile());
			});
		
		AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackCompleteAfterCreateChangeSet = project.getTasks().create(
				"awsCfnWaitStackCompleteAfterCreateChangeSet", AmazonCloudFormationWaitStackStatusTask.class,
				task -> {
					task.setDescription(
							"Wait cfn stack for CREATE_COMPLETE, UPDATE_COMPLETE or REVIEW_IN_PROGRESS status.");
					task.mustRunAfter(awsCfnCreateChangeSet);
					task.setSuccessStatuses(Arrays.asList("CREATE_COMPLETE", "UPDATE_COMPLETE", "REVIEW_IN_PROGRESS"));
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
				});
		
		AmazonCloudFormationWaitChangeSetStatusTask awsCfnWaitCreateChangeSetComplete = project.getTasks().create(
				"awsWaitCreateChangeSetComplete", AmazonCloudFormationWaitChangeSetStatusTask.class,
				task -> {
					task.setDescription("Wait cfn change set for CREATE_COMPLETE status");
					task.mustRunAfter(awsCfnWaitStackCompleteAfterCreateChangeSet);
					task.setSuccessStatuses(Arrays.asList("CREATE_COMPLETE"));
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
				});
		
		AmazonCloudFormationExecuteChangeSetTask awsCfnExecuteChangeSet = project.getTasks()
			.create("awsCfnExecuteChangeSet", AmazonCloudFormationExecuteChangeSetTask.class, task -> {
				task.mustRunAfter(awsCfnWaitCreateChangeSetComplete);
				task.setDescription("execute latest cfn change set.");
				task.conventionMapping("stackName", () -> cfnExt.getStackName());
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
							task.mustRunAfter(awsCfnExecuteChangeSet);
							task.setSuccessStatuses(Arrays.asList("CREATE_COMPLETE", "UPDATE_COMPLETE"));
							task.conventionMapping("stackName", () -> cfnExt.getStackName());
						});
		
		project.getTasks().create("awsCfnMigrateStackAndWaitCompleted")
			.dependsOn(awsCfnMigrateStack, awsCfnWaitStackComplete)
			.setDescription("Create/Migrate cfn stack, and wait stack for CREATE_COMPLETE or UPDATE_COMPLETE status.");
		
		project.getTasks().create("awsCfnDeploy")
			.dependsOn(awsCfnCreateChangeSet, awsCfnWaitStackCompleteAfterCreateChangeSet,
					awsCfnWaitCreateChangeSetComplete, awsCfnExecuteChangeSet,
					awsCfnWaitStackComplete)
			.setDescription(
					"Create and execute a change set, waiting for completion. Useful for deploying CloudFormation templates"
							+ "containing transforms.");
		
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
		
		project.getTasks().create("awsCfnUpdateStackTerminationProtection",
				AmazonCloudFormationStackTerminationProtectionTask.class, task -> {
					task.setDescription("Update CloudFormation stack termination protection");
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
					task.conventionMapping("terminationProtected", () -> cfnExt.isTerminationProtected());
				});
		
		project.getTasks().create("awsCfnSetStackPolicy",
				AmazonCloudFormationStackPolicyTask.class, task -> {
					task.setDescription("Set CloudFormation stack policy");
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
					task.conventionMapping("cfnStackPolicyUrl", () -> cfnExt.getStackPolicyURL());
					task.conventionMapping("cfnStackPolicyFile", () -> cfnExt.getStackPolicyFile());
				});
		
	}
	//CHECKSTYLE:ON
	
	private String createKey(String name, Object version, String prefix) {
		String path = name.substring(FilenameUtils.getPrefix(name).length());
		String baseName = FilenameUtils.getBaseName(name);
		String extension = FilenameUtils.getExtension(name);
		return String.format(Locale.ENGLISH, "%s/%s/%s-%s-%s%s", new Object[] {
			prefix,
			path,
			baseName,
			version,
			createTimestamp(),
			extension.length() > 0 ? "." + extension : ""
		});
	}
	
	private String createTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'_'HHmmss", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(new Date());
	}
}
