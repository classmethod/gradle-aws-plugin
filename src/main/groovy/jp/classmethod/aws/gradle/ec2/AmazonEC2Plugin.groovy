/*
 * Copyright 2013-2014 Classmethod, Inc.
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
package jp.classmethod.aws.gradle.ec2

import groovy.lang.Lazy;
import jp.classmethod.aws.gradle.AwsPluginExtension

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.regions.*
import com.amazonaws.services.ec2.*
import com.amazonaws.services.ec2.model.*
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*


class AmazonEC2Plugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			project.extensions.create(AmazonEC2PluginExtension.NAME, AmazonEC2PluginExtension, project)
		}
	}
}

class AmazonEC2PluginExtension {
	
	public static final NAME = 'ec2'
	
	Project project
	String profileName
	Region region
		
	@Lazy
	AmazonEC2 ec2 = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AmazonEC2Client, region, profileName)
	}()
	
	AmazonEC2PluginExtension(Project project) {
		this.project = project;
	}
}
