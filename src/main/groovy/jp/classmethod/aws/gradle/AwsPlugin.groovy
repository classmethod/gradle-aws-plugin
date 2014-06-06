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
package jp.classmethod.aws.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.auth.*
import com.amazonaws.regions.*
import com.amazonaws.internal.StaticCredentialsProvider

/**
 * A plugin which configures a AWS project.
 */
class AwsPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			project.extensions.create(AwsPluginExtension.NAME, AwsPluginExtension, project)
		}
	}
}

class AwsPluginExtension {
	
	public static final NAME = 'aws'
	
	Project project;
	
	String accessKeyId
	
	String secretKey
	
	Region region = Region.getRegion(Regions.US_EAST_1)
	
	
	AwsPluginExtension(Project project) {
		this.project = project;
	}

	def void setRegion(String r) {
		region = RegionUtils.getRegion(r)
	}
	
	def void setRegion(Regions r) {
		region = RegionUtils.getRegion(r.name)
	}
	
	def AWSCredentialsProvider newCredentialsProvider(String accessKeyId, String secretKey) {
		return new AWSCredentialsProviderChain(
			new StaticCredentialsProvider((accessKeyId && secretKey) ?
				new BasicAWSCredentials(accessKeyId, secretKey) : null),
			new StaticCredentialsProvider((this.accessKeyId && this.secretKey) ?
				new BasicAWSCredentials(this.accessKeyId, this.secretKey) : null)
		)
	}
	
	def <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, Region region = null, String accessKeyId = null, String secretKey = null) {
		if (region == null) {
			if (this.region == null) throw new IllegalStateException('default region is null')
			region = this.region
		}
		
		return region.createClient(serviceClass, newCredentialsProvider(accessKeyId, secretKey), null)
	}
}
