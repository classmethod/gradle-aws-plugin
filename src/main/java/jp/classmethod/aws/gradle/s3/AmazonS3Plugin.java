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
package jp.classmethod.aws.gradle.s3;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import jp.classmethod.aws.gradle.AwsPlugin;
import jp.classmethod.aws.gradle.AwsPluginExtension;

public class AmazonS3Plugin implements Plugin<Project> {
	
	public void apply(Project project) {
		project.getPluginManager().apply(AwsPlugin.class);
		project.getExtensions().getByType(AwsPluginExtension.class).asExtensionAware().getExtensions()
			.create(AmazonS3PluginExtension.NAME, AmazonS3PluginExtension.class, project);
	}
}
