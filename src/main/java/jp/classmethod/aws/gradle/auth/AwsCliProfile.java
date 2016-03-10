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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.amazonaws.auth.AWSCredentialsProvider;

import org.apache.http.annotation.Immutable;

/**
 * TODO for daisuke
 * 
 * @since #version#
 * @version $Id$
 * @author daisuke
 */
@Immutable
@RequiredArgsConstructor
public class AwsCliProfile {
	
	/** Property name for specifying the Amazon AWS Access Key */
	static final String AWS_ACCESS_KEY_ID = "aws_access_key_id";
	
	/** Property name for specifying the Amazon AWS Secret Access Key */
	static final String AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";
	
	/** Property name for specifying the Amazon AWS Session Token */
	static final String AWS_SESSION_TOKEN = "aws_session_token";
	
	static final String AWS_ROLE_ARN = "role_arn";
	
	static final String AWS_SOURCE_PROFILE = "source_profile";
	
	static final String AWS_ROLE_SESSION_NAME = "role_session_name";
	
	/** The name of this profile */
	@Getter
	private final String profileName;
	
	/** Holds the AWS Credentials for the profile. */
	@Getter
	private final AWSCredentialsProvider awsCredentialsProvider;
}
