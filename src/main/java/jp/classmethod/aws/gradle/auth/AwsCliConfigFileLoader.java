/*
 * Copyright 2013 Daisuke Miyamoto.
 * Created on 2016/03/08
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package jp.classmethod.aws.gradle.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.internal.AbstractProfilesConfigFileScanner;
import com.amazonaws.internal.StaticCredentialsProvider;

/**
 * TODO for daisuke
 * 
 * @since #version#
 * @version $Id$
 * @author daisuke
 */
public class AwsCliConfigFileLoader {
	
	public static Map<String, AwsCliProfile> loadProfiles(File file) {
		if (file == null) {
			throw new IllegalArgumentException("Unable to load AWS profiles: specified file is null.");
		}
		
		if (file.exists() == false || file.isFile() == false) {
			throw new IllegalArgumentException("AWS credential profiles file not found in the given path: "
					+ file.getAbsolutePath());
		}
		
		try (FileInputStream fis = new FileInputStream(file)) {
			return loadProfiles(fis);
		} catch (IOException ioe) {
			throw new AmazonClientException(
					"Unable to load AWS credential profiles file at: " + file.getAbsolutePath(), ioe);
		}
	}
	
	/**
	 * Loads the credential profiles from the given input stream.
	 *
	 * @param is input stream from where the profile details are read.
	 * @return
	 * @throws IOException
	 */
	private static Map<String, AwsCliProfile> loadProfiles(InputStream is) throws IOException {
		ProfilesConfigFileLoaderHelper helper = new ProfilesConfigFileLoaderHelper();
		try (Scanner s = new Scanner(is)) {
			Map<String, Map<String, String>> allProfileProperties = helper.parseProfileProperties(s);
			
			// Convert the loaded property map to credential objects
			Map<String, AwsCliProfile> profilesByName = new LinkedHashMap<>();
			
			for (Entry<String, Map<String, String>> entry : allProfileProperties.entrySet()) {
				String profileName = entry.getKey();
				Map<String, String> properties = entry.getValue();
				
				if (profileName.equals(AwsCliConfigFile.DEFAULT_PROFILE_NAME) == false) {
					if (profileName.startsWith("profile ") == false) {
						continue;
					}
					profileName = profileName.substring("profile ".length());
				}
				
				String accessKey = properties.get(AwsCliProfile.AWS_ACCESS_KEY_ID);
				String secretKey = properties.get(AwsCliProfile.AWS_SECRET_ACCESS_KEY);
				String sessionToken = properties.get(AwsCliProfile.AWS_SESSION_TOKEN);
				String roleArn = properties.get(AwsCliProfile.AWS_ROLE_ARN);
				String sourceProfile = properties.get(AwsCliProfile.AWS_SOURCE_PROFILE);
				String roleSessionName = properties.get(AwsCliProfile.AWS_ROLE_SESSION_NAME);
				
				assertParameterNotEmpty(profileName,
						"Unable to load credentials into profile: ProfileName is empty.");
				if (accessKey != null && secretKey != null) {
					if (sessionToken == null) {
						AWSCredentialsProvider cp = new StaticCredentialsProvider(
								new BasicAWSCredentials(accessKey, secretKey));
						profilesByName.put(profileName, new AwsCliProfile(profileName, cp));
					} else {
						if (sessionToken.isEmpty()) {
							String msg = String.format(
									"Unable to load credentials into profile [%s]: AWS Session Token is empty.",
									profileName);
							throw new AmazonClientException(msg);
						}
						AWSCredentialsProvider cp = new StaticCredentialsProvider(
								new BasicSessionCredentials(accessKey, secretKey, sessionToken));
						profilesByName.put(profileName, new AwsCliProfile(profileName, cp));
					}
				} else if (roleArn != null && sourceProfile != null) {
					if (roleSessionName == null) {
						roleSessionName = "defaultsession";
					}
					AWSCredentialsProvider source = new AWSCredentialsProviderChain(
							new AwsCliConfigProfileCredentialsProvider(sourceProfile),
							new ProfileCredentialsProvider(sourceProfile));
					AWSCredentialsProvider cp = new STSAssumeRoleSessionCredentialsProvider(
							source, roleArn, roleSessionName);
					profilesByName.put(profileName, new AwsCliProfile(profileName, cp));
				}
			}
			
			return profilesByName;
		}
	}
	
	/**
	 * <p>
	 * Asserts that the specified parameter value is neither <code>empty</code>
	 * nor null, and if it is, throws an <code>AmazonClientException</code> with
	 * the specified error message.
	 * </p>
	 *
	 * @param parameterValue
	 *            The parameter value being checked.
	 * @param errorMessage
	 *            The error message to include in the AmazonClientException if
	 *            the specified parameter value is empty.
	 */
	private static void assertParameterNotEmpty(String parameterValue, String errorMessage) {
		if (parameterValue == null || parameterValue.isEmpty()) {
			throw new AmazonClientException(errorMessage);
		}
	}
	
	
	/**
	 * Implementation of AbstractProfilesConfigFileScanner that groups profile
	 * properties into a map while scanning through the credentials profile.
	 */
	private static class ProfilesConfigFileLoaderHelper extends AbstractProfilesConfigFileScanner {
		
		/**
		 * Map from the parsed profile name to the map of all the property values
		 * included the specific profile
		 */
		protected final Map<String, Map<String, String>> allProfileProperties = new LinkedHashMap<>();
		
		
		/**
		 * Parses the input and returns a map of all the profile properties.
		 */
		public Map<String, Map<String, String>> parseProfileProperties(Scanner scanner) {
			allProfileProperties.clear();
			run(scanner);
			return new LinkedHashMap<>(allProfileProperties);
		}
		
		@Override
		protected void onEmptyOrCommentLine(String profileName, String line) {
			// Ignore empty or comment line
		}
		
		@Override
		protected void onProfileStartingLine(String newProfileName, String line) {
			// If the same profile name has already been declared, clobber the
			// previous one
			allProfileProperties.put(newProfileName, new HashMap<String, String>());
		}
		
		@Override
		protected void onProfileEndingLine(String prevProfileName) {
			// No-op
		}
		
		@Override
		protected void onProfileProperty(String profileName,
				String propertyKey, String propertyValue,
				boolean isSupportedProperty, String line) {
			
			// Not strictly necessary, since the abstract super class guarantees
			// onProfileStartingLine is always invoked before this method.
			// Just to be safe...
			if (allProfileProperties.get(profileName) == null) {
				allProfileProperties.put(profileName, new HashMap<String, String>());
			}
			
			Map<String, String> properties = allProfileProperties.get(profileName);
			
			if (properties.containsKey(propertyKey)) {
				throw new IllegalArgumentException(
						"Duplicate property values for [" + propertyKey + "].");
			}
			
			properties.put(propertyKey, propertyValue);
		}
		
		@Override
		protected void onEndOfFile() {
			// No-op
		}
	}
}
