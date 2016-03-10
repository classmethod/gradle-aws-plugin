/*
 * Copyright 2013-2016 Classmethod, Inc.
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
package jp.classmethod.aws.gradle.auth;

import java.util.concurrent.Semaphore;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.util.StringUtils;

/**
 * TODO for daisuke
 * 
 * @since 0.24
 * @version $Id$
 * @author daisuke
 */
public class AwsCliConfigProfileCredentialsProvider implements AWSCredentialsProvider {
	
	/**
	 * Default refresh interval
	 */
	private static final long DEFAULT_REFRESH_INTERVAL_NANOS = 5 * 60 * 1000 * 1000 * 1000L;
	
	/**
	 * Default force reload interval
	 */
	private static final long DEFAULT_FORCE_RELOAD_INTERVAL_NANOS = 2 * DEFAULT_REFRESH_INTERVAL_NANOS;
	
	/**
	 * The credential profiles file from which this provider loads the security
	 * credentials.
	 * Lazily loaded by the double-check idiom.
	 */
	private AwsCliConfigFile cliConfigFile;
	
	/**
	 * When the profiles file was last refreshed.
	 */
	private volatile long lastRefreshed;
	
	/** The name of the credential profile */
	private final String profileName;
	
	/**
	 * Used to have only one thread block on refresh, for applications making
	 * at least one call every REFRESH_INTERVAL_NANOS.
	 */
	private final Semaphore refreshSemaphore = new Semaphore(1);
	
	/**
	 * Refresh interval. Defaults to {@link #DEFAULT_REFRESH_INTERVAL_NANOS}
	 */
	private long awsCliConfigRefreshIntervalNanos = DEFAULT_REFRESH_INTERVAL_NANOS;
	
	/**
	 * Force reload interval. Defaults to {@link #DEFAULT_FORCE_RELOAD_INTERVAL_NANOS}
	 */
	private long awsCliConfigRefreshForceIntervalNanos = DEFAULT_FORCE_RELOAD_INTERVAL_NANOS;
	
	
	/**
	 * インスタンスを生成する。
	 */
	public AwsCliConfigProfileCredentialsProvider() {
		this(null);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param profileName
	 */
	public AwsCliConfigProfileCredentialsProvider(String profileName) {
		this((AwsCliConfigFile) null, profileName);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param cliConfigFilePath
	 * @param profilesConfigFilePath
	 * @param profileName
	 */
	public AwsCliConfigProfileCredentialsProvider(String cliConfigFilePath, String profileName) {
		this(new AwsCliConfigFile(cliConfigFilePath), profileName);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param cliConfigFile
	 * @param profilesConfigFile
	 * @param profileName
	 */
	public AwsCliConfigProfileCredentialsProvider(AwsCliConfigFile cliConfigFile,
			String profileName) {
		this.cliConfigFile = cliConfigFile;
		if (this.cliConfigFile != null) {
			lastRefreshed = System.nanoTime();
		}
		if (profileName == null) {
			String profileEnvVarOverride = System.getenv(ProfilesConfigFile.AWS_PROFILE_ENVIRONMENT_VARIABLE);
			profileEnvVarOverride = StringUtils.trim(profileEnvVarOverride);
			if (!StringUtils.isNullOrEmpty(profileEnvVarOverride)) {
				this.profileName = profileEnvVarOverride;
			} else {
				String profileSysPropOverride = System.getProperty(ProfilesConfigFile.AWS_PROFILE_SYSTEM_PROPERTY);
				profileSysPropOverride = StringUtils.trim(profileSysPropOverride);
				if (!StringUtils.isNullOrEmpty(profileSysPropOverride)) {
					this.profileName = profileSysPropOverride;
				} else {
					this.profileName = ProfilesConfigFile.DEFAULT_PROFILE_NAME;
				}
			}
		} else {
			this.profileName = profileName;
		}
	}
	
	@Override
	public AWSCredentials getCredentials() {
		if (cliConfigFile == null) {
			synchronized (this) {
				if (cliConfigFile == null) {
					cliConfigFile = new AwsCliConfigFile();
					lastRefreshed = System.nanoTime();
				}
			}
		}
		
		// Periodically check if the file on disk has been modified
		// since we last read it.
		//
		// For active applications, only have one thread block.
		// For applications that use this method in bursts, ensure the
		// credentials are never too stale.
		long now = System.nanoTime();
		long age = now - lastRefreshed;
		if (age > awsCliConfigRefreshForceIntervalNanos) {
			refresh();
		} else if (age > awsCliConfigRefreshIntervalNanos) {
			if (refreshSemaphore.tryAcquire()) {
				try {
					refresh();
				} finally {
					refreshSemaphore.release();
				}
			}
		}
		
		AWSCredentialsProvider cp = cliConfigFile.getCredentialsProvider(profileName);
		if (cp != null) {
			return cp.getCredentials();
		}
		return null;
	}
	
	@Override
	public void refresh() {
		cliConfigFile.refresh();
		lastRefreshed = System.nanoTime();
	}
}
