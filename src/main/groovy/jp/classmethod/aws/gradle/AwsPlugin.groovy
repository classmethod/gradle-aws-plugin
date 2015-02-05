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

import com.amazonaws.AmazonWebServiceClient
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.RegionUtils
import com.amazonaws.regions.Regions

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin which configures a AWS project.
 */
class AwsPlugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			extensions.create(AwsPluginExtension.NAME, AwsPluginExtension, project)
		}
	}
}

class AwsPluginExtension {
	
	public static final NAME = 'aws'
	
	Project project
	
	String profileName
	
	Region region = Region.getRegion(Regions.US_EAST_1)
	
	
	AwsPluginExtension(Project project) {
		this.project = project
	}

	void setRegion(String r) {
		region = RegionUtils.getRegion(r)
	}
	
	void setRegion(Regions r) {
		region = RegionUtils.getRegion(r.name)
	}
	
	def AWSCredentialsProvider newCredentialsProvider(String profileName) {
		return new AWSCredentialsProviderChain(
			new EnvironmentVariableCredentialsProvider(),
			new SystemPropertiesCredentialsProvider(),
			profileName ? new ProfileCredentialsProvider(profileName) : new AWSCredentialsProvider() {
				void refresh() {}
				AWSCredentials getCredentials() { null }
			},
			new ProfileCredentialsProvider(this.profileName),
			new InstanceProfileCredentialsProvider()
		)
	}
	
	def <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, Region region = null, String profileName = null) {
		if (region == null) {
			if (this.region == null) {
				throw new IllegalStateException('default region is null')
			}
			region = this.region
		}

		def credentialsProvider = newCredentialsProvider(profileName)
		return region.createClient(serviceClass, credentialsProvider, null)
	}
}
