/*
 * Copyright 2013-2015 Classmethod, Inc.
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
package jp.classmethod.aws.gradle;

import java.lang.reflect.Constructor;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.google.common.base.Strings;

public class AwsPluginExtension {
	
	public static final String NAME = "aws";
	
	private static final AWSCredentialsProvider EMPTY = new AWSCredentialsProvider() {
		
		public void refresh() {
		}
		
		public AWSCredentials getCredentials() {
			return null;
		}
	};

	@Getter
	@Setter
	private Project project;
	
	@Getter
	@Setter
	private String profileName = "default";
	
	@Getter
	@Setter
	private String region = Regions.US_EAST_1.getName();
	
	
	public AwsPluginExtension(Project project) {
		this.project = project;
	}
	
	public AWSCredentialsProvider newCredentialsProvider(String profileName) {
		return new AWSCredentialsProviderChain(
				new EnvironmentVariableCredentialsProvider(),
				new SystemPropertiesCredentialsProvider(),
				Strings.isNullOrEmpty(profileName) == false ? new ProfileCredentialsProvider(profileName) : EMPTY,
				new ProfileCredentialsProvider(this.profileName),
				new InstanceProfileCredentialsProvider());
	}
	
	public <T extends AmazonWebServiceClient>T createClient(Class<T> serviceClass, String profileName) {
		if (profileName == null) {
			if (this.profileName == null) {
				throw new IllegalStateException("default profileName is null");
			}
			profileName = this.profileName;
		}
		
		AWSCredentialsProvider credentialsProvider = newCredentialsProvider(profileName);
		return createClient(serviceClass, credentialsProvider, null);
	}
	
	private static <T extends AmazonWebServiceClient>T createClient(Class<T> serviceClass,
			AWSCredentialsProvider credentials, ClientConfiguration config) {
		Constructor<T> constructor;
		T client;
		try {
			if (credentials == null && config == null) {
				constructor = serviceClass.getConstructor();
				client = constructor.newInstance();
			} else if (credentials == null) {
				constructor = serviceClass.getConstructor(ClientConfiguration.class);
				client = constructor.newInstance(config);
			} else if (config == null) {
				constructor = serviceClass.getConstructor(AWSCredentialsProvider.class);
				client = constructor.newInstance(credentials);
			} else {
				constructor = serviceClass.getConstructor(AWSCredentialsProvider.class, ClientConfiguration.class);
				client = constructor.newInstance(credentials, config);
			}
			
			return client;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't instantiate instance of " + serviceClass, e);
		}
	}
	
	public Region getActiveRegion(String clientRegion) {
		if (clientRegion != null) {
			return RegionUtils.getRegion(clientRegion);
		}
		if (this.region == null) {
			throw new IllegalStateException("default region is null");
		}
		return RegionUtils.getRegion(region);
	}
	
	public String getActiveProfileName(String clientProfileName) {
		if (clientProfileName != null) {
			return clientProfileName;
		}
		if (this.profileName == null) {
			throw new IllegalStateException("default profileName is null");
		}
		return profileName;
	}
	
	public String getAccountId() {
		String arn = getUserArn(); // ex. arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/Bob
		return arn.split(":")[4];
	}
	
	public String getUserArn() {
		AmazonIdentityManagement iam = createClient(AmazonIdentityManagementClient.class, profileName);
		try {
			GetUserResult getUserResult = iam.getUser();
			return getUserResult.getUser().getArn();
		} catch (AmazonServiceException e) {
			if (e.getErrorCode().equals("AccessDenied") == false) {
				throw e;
			}
			String msg = e.getMessage();
			int arnIdx = msg.indexOf("arn:aws");
			if (arnIdx == -1) {
				throw e;
			}
			int arnSpace = msg.indexOf(" ", arnIdx);
			return msg.substring(arnIdx, arnSpace);
		}
	}
}
