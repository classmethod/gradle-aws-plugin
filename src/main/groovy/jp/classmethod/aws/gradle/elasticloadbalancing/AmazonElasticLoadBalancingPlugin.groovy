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
package jp.classmethod.aws.gradle.elasticloadbalancing

import groovy.lang.Lazy;
import jp.classmethod.aws.gradle.AwsPluginExtension

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.regions.*
import com.amazonaws.services.elasticloadbalancing.*
import com.amazonaws.services.elasticloadbalancing.model.*


class AmazonElasticLoadBalancingPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			project.extensions.create(AmazonELBPluginExtension.NAME, AmazonELBPluginExtension, project)
		}
	}
}

class AmazonELBPluginExtension {
	
	public static final NAME = 'elb'
	
	Project project
	String profileName
	Region region
		
	@Lazy
	AmazonElasticLoadBalancing elb = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AmazonElasticLoadBalancingClient, region, profileName)
	}()
	
	AmazonELBPluginExtension(Project project) {
		this.project = project;
	}
}
