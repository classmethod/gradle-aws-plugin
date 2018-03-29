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
package jp.classmethod.aws.gradle.elasticbeanstalk;

import java.util.Arrays;
import java.util.Collections;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

import com.google.common.base.Strings;

import jp.classmethod.aws.gradle.AwsPlugin;
import jp.classmethod.aws.gradle.AwsPluginExtension;
import jp.classmethod.aws.gradle.s3.AmazonS3Plugin;
import jp.classmethod.aws.gradle.s3.AmazonS3ProgressiveFileUploadTask;

/**
 * A plugin which configures a AWS Elastic Beanstalk project.
 */
public class AwsBeanstalkPlugin implements Plugin<Project> {
	
	public void apply(Project project) {
		project.getPluginManager().apply(AwsPlugin.class);
		project.getPluginManager().apply(AmazonS3Plugin.class);
		project.getExtensions().getByType(AwsPluginExtension.class).asExtensionAware().getExtensions()
			.create(AwsBeanstalkPluginExtension.NAME, AwsBeanstalkPluginExtension.class, project);
		applyTasks(project);
	}
	
	private void applyTasks(final Project project) { // NOPMD
		AwsBeanstalkPluginExtension ebExt = project.getExtensions().findByType(AwsPluginExtension.class)
				.asExtensionAware().getExtensions().findByType(AwsBeanstalkPluginExtension.class);
		
		AWSElasticBeanstalkCreateApplicationTask awsEbMigrateApplication = project.getTasks()
			.create("awsEbMigrateApplication", AWSElasticBeanstalkCreateApplicationTask.class, task -> {
				task.doFirst(t -> {
					task.setAppName(ebExt.getAppName());
					task.setAppDesc(ebExt.getAppDesc());
				});
			});
		
		AmazonS3ProgressiveFileUploadTask awsUploadWar = project.getTasks()
			.create("awsEbUploadBundle", AmazonS3ProgressiveFileUploadTask.class, task -> {
				WarPlugin war = project.getPlugins().findPlugin(WarPlugin.class);
				War warTask = war == null ? null : (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
				if (war != null) {
					task.dependsOn(warTask);
				}
				task.onlyIf(t -> ebExt.getVersion().getFile() != null || war != null);
				task.doFirst(t -> {
					task.setBucketName(ebExt.getVersion().getBucket());
					task.setKey(ebExt.getVersion().getKey());
					if (warTask != null && ebExt.getVersion().getFile() == null) {
						task.setFile(warTask.getArchivePath());
					} else {
						task.setFile(ebExt.getVersion().getFile());
					}
				});
			});
		
		AWSElasticBeanstalkCreateApplicationVersionTask awsEbCreateApplicationVersion = project.getTasks()
			.create("awsEbCreateApplicationVersion", AWSElasticBeanstalkCreateApplicationVersionTask.class, task -> {
				task.dependsOn(awsEbMigrateApplication, awsUploadWar);
				task.doFirst(t -> {
					task.setAppName(ebExt.getAppName());
					task.setVersionLabel(ebExt.getVersion().getLabel());
					task.setBucketName(ebExt.getVersion().getBucket());
					task.setKey(ebExt.getVersion().getKey());
				});
			});
		
		AWSElasticBeanstalkCreateConfigurationTemplateTask awsEbMigrateConfigurationTemplates = project.getTasks()
			.create("awsEbMigrateConfigurationTemplates", AWSElasticBeanstalkCreateConfigurationTemplateTask.class,
					task -> {
						task.dependsOn(awsEbMigrateApplication);
						task.doFirst(t -> {
							task.setAppName(ebExt.getAppName());
							task.setConfigurationTemplates(ebExt.getConfigurationTemplates());
						});
					});
		
		AWSElasticBeanstalkCreateEnvironmentTask awsEbMigrateEnvironment = project.getTasks()
			.create("awsEbMigrateEnvironment", AWSElasticBeanstalkCreateEnvironmentTask.class, task -> {
				task.dependsOn(awsEbMigrateConfigurationTemplates, awsEbCreateApplicationVersion);
				task.doFirst(t -> {
					task.setAppName(ebExt.getAppName());
					task.setEnvName(ebExt.getEnvironment().getEnvName());
					task.setEnvDesc(ebExt.getEnvironment().getEnvDesc());
					task.setTemplateName(ebExt.getEnvironment().getTemplateName());
					task.setVersionLabel(ebExt.getEnvironment().getVersionLabel());
					task.setTier(ebExt.getTier() != null ? ebExt.getTier() : Tier.WebServer);
					if (Strings.isNullOrEmpty(ebExt.getEnvironment().getCnamePrefix()) == false) {
						task.setCnamePrefix(ebExt.getEnvironment().getCnamePrefix());
					}
					if (ebExt.getEnvironment().getTags() != null) {
						task.setTags(ebExt.getEnvironment().getTags());
					}
				});
			});
		
		AWSElasticBeanstalkTerminateEnvironmentTask awsEbTerminateEnvironment = project.getTasks()
			.create("awsEbTerminateEnvironment", AWSElasticBeanstalkTerminateEnvironmentTask.class, task -> {
				task.doFirst(t -> {
					task.setAppName(ebExt.getAppName());
					task.setEnvName(ebExt.getEnvironment().getEnvName());
				});
			});
		
		project.getTasks().create("awsEbWaitEnvironmentReady", AWSElasticBeanstalkWaitEnvironmentStatusTask.class,
				task -> {
					task.mustRunAfter(awsEbMigrateEnvironment);
					task.doFirst(t -> {
						task.setAppName(ebExt.getAppName());
						task.setEnvName(ebExt.getEnvironment().getEnvName());
					});
				});
		
		AWSElasticBeanstalkWaitEnvironmentStatusTask awsEbWaitEnvironmentTerminated = project.getTasks()
			.create("awsEbWaitEnvironmentTerminated", AWSElasticBeanstalkWaitEnvironmentStatusTask.class, task -> {
				task.mustRunAfter(awsEbTerminateEnvironment);
				task.doFirst(t -> {
					task.setAppName(ebExt.getAppName());
					task.setEnvName(ebExt.getEnvironment().getEnvName());
					task.setSuccessStatuses(Collections.singletonList("Terminated"));
					task.setWaitStatuses(Arrays.asList(
							"Launching",
							"Updating",
							"Terminating",
							"Ready"));
				});
			});
		
		Task awsEbTerminateEnvironmentAndWaitTerminated = project.getTasks()
			.create("awsEbTerminateEnvironmentAndWaitTerminated")
			.dependsOn(awsEbTerminateEnvironment, awsEbWaitEnvironmentTerminated);
		
		project.getTasks().create("awsEbCleanupApplicationVersions",
				AWSElasticBeanstalkCleanupApplicationVersionTask.class, task -> {
					task.doFirst(t -> {
						task.setAppName(ebExt.getAppName());
					});
				});
		
		project.getTasks().create("awsEbDeleteApplication", AWSElasticBeanstalkDeleteApplicationTask.class, task -> {
			task.dependsOn(awsEbTerminateEnvironmentAndWaitTerminated);
			task.doFirst(t -> {
				task.setAppName(ebExt.getAppName());
			});
		});
	}
}
