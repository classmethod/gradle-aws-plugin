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
import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO for daisuke
 * 
 * @since #version#
 * @version $Id$
 * @author daisuke
 */
public class AwsCliConfigFile {
	
	private static Logger LOG = LoggerFactory.getLogger(AwsCliConfigFile.class);
	
	/** Environment variable name for overriding the default AWS profile */
	public static final String AWSCLI_CONFIG_ENVIRONMENT_VARIABLE = "AWSCLI_CONFIG";
	
	/** System property name for overriding the default AWS profile */
	public static final String AWSCLI_CONFIG_SYSTEM_PROPERTY = "awscli.config";
	
	/** Environment variable specifying an alternate location for the AWS credential profiles file */
	private static final String AWSCLI_CONFIG_FILE_ENVIRONMENT_VARIABLE = "AWSCLI_CONFIG_FILE";
	
	/** File name of the default location where the credential profiles are saved */
	private static final String AWSCLI_CONFIG_FILENAME = "config";
	
	/** Name of the default profile as specified in the configuration file. */
	public static final String DEFAULT_PROFILE_NAME = "default";
	
	private final File profileFile;
	
	private volatile Map<String, AwsCliProfile> profilesByName;
	
	private volatile long profileFileLastModified;
	
	
	/**
	 * Loads the AWS credential profiles file from the default location
	 * (~/.aws/credentials) or from an alternate location if
	 * <code>AWS_CREDENTIAL_PROFILES_FILE</code> is set.
	 */
	public AwsCliConfigFile() throws AmazonClientException {
		this(getCredentialProfilesFile());
	}
	
	/**
	 * Loads the AWS credential profiles from the file. The path of the file is
	 * specified as a parameter to the constructor.
	 */
	public AwsCliConfigFile(String filePath) {
		this(new File(validateFilePath(filePath)));
	}
	
	private static String validateFilePath(String filePath) {
		if (filePath == null) {
			throw new IllegalArgumentException("Unable to load AWS profiles: specified file path is null.");
		}
		return filePath;
	}
	
	/**
	 * Loads the AWS credential profiles from the file. The reference to the
	 * file is specified as a parameter to the constructor.
	 */
	public AwsCliConfigFile(File file) throws AmazonClientException {
		profileFile = file;
		profileFileLastModified = file.lastModified();
		profilesByName = loadProfiles(profileFile);
	}
	
	/**
	 * Returns the AWS credentials for the specified profile.
	 */
	public AWSCredentialsProvider getCredentialsProvider(String profile) {
		AwsCliProfile p = profilesByName.get(profile);
		if (p == null) {
			throw new IllegalArgumentException("No AWS profile named '" + profile + "'");
		}
		return p.getAwsCredentialsProvider();
	}
	
	/**
	 * Reread data from disk.
	 */
	public void refresh() {
		if (profileFile.lastModified() > profileFileLastModified) {
			profileFileLastModified = profileFile.lastModified();
			profilesByName = loadProfiles(profileFile);
		}
	}
	
	/**
	 * Returns all the profiles declared in this config file.
	 */
	public Map<String, AwsCliProfile> getAllProfiles() {
		return new LinkedHashMap<>(profilesByName);
	}
	
	private static File getCredentialProfilesFile() {
		String legacyConfigFileOverride = System.getenv(AWSCLI_CONFIG_FILE_ENVIRONMENT_VARIABLE);
		
		if (legacyConfigFileOverride != null) {
			LOG.debug("Loading AWS credential profiles from overridden file: " + legacyConfigFileOverride);
			return new File(legacyConfigFileOverride);
		}
		
		String userHome = System.getProperty("user.home");
		if (userHome == null) {
			throw new AmazonClientException("Unable to load AWS profiles: "
					+ "'user.home' System property is not set.");
		}
		
		File awsDirectory = new File(userHome, ".aws");
		return new File(awsDirectory, AWSCLI_CONFIG_FILENAME);
	}
	
	private Map<String, AwsCliProfile> loadProfiles(File file) {
		return new LinkedHashMap<>(AwsCliConfigFileLoader.loadProfiles(file));
	}
	
}
