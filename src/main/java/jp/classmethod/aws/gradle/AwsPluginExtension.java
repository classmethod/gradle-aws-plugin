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
package jp.classmethod.aws.gradle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.google.common.base.Strings;

@RequiredArgsConstructor
public class AwsPluginExtension {
	
	public static final String NAME = "aws";
	
	@Getter
	private final Project project;
	
	@Getter
	@Setter
	private String profileName;
	
	@Getter
	@Setter
	private String region = Regions.US_EAST_1.getName();
	
	@Setter
	private String proxyHost;
	
	@Setter
	private int proxyPort = -1;
	
	@Setter
	private AWSCredentialsProvider credentialsProvider;
	
	
	public AWSCredentialsProvider newCredentialsProvider(String profileName) {
		if (credentialsProvider != null) {
			return credentialsProvider;
		}
		String profileNameToUse = profileName != null ? profileName : this.profileName;
		if (Strings.isNullOrEmpty(profileNameToUse) == false) {
			List<AWSCredentialsProvider> providers = new ArrayList<AWSCredentialsProvider>();
			providers.add(new EnvironmentVariableCredentialsProvider());
			providers.add(new SystemPropertiesCredentialsProvider());
			providers.add(new ProfileCredentialsProvider(profileNameToUse));
			providers.add(new EC2ContainerCredentialsProviderWrapper());
			return new AWSCredentialsProviderChain(providers);
		}
		return DefaultAWSCredentialsProviderChain.getInstance();
	}
	
	public <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, String profileName) {
		return createClient(serviceClass, profileName, null);
	}
	
	public <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, String profileName,
			ClientConfiguration config) {
		AWSCredentialsProvider credentialsProvider = newCredentialsProvider(profileName);
		ClientConfiguration configToUse = config == null ? new ClientConfiguration() : config;
		if (this.proxyHost != null && this.proxyPort > 0) {
			configToUse.setProxyHost(this.proxyHost);
			configToUse.setProxyPort(this.proxyPort);
		}
		return createClient(serviceClass, credentialsProvider, configToUse);
	}
	
	private static <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass,
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
		} catch (ReflectiveOperationException e) {
			throw new GradleException("Couldn't instantiate instance of " + serviceClass, e);
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
		try {
			AWSSecurityTokenService sts = createClient(AWSSecurityTokenServiceClient.class, profileName);
			sts.setRegion(getActiveRegion(region));
			return sts.getCallerIdentity(new GetCallerIdentityRequest()).getAccount();
		} catch (SdkClientException e) {
			project.getLogger().lifecycle("AWS credentials not configured!");
			return null;
		}
		
	}
	
	public String getUserArn() {
		try {
			AWSSecurityTokenService sts = createClient(AWSSecurityTokenServiceClient.class, profileName);
			sts.setRegion(getActiveRegion(region));
			return sts.getCallerIdentity(new GetCallerIdentityRequest()).getArn();
		} catch (SdkClientException e) {
			project.getLogger().lifecycle("AWS credentials not configured!");
			return null;
		}
		
	}
}
