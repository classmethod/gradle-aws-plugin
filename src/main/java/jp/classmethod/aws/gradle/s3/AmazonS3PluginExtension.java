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
package jp.classmethod.aws.gradle.s3;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


public class AmazonS3PluginExtension {
	
	public static final String NAME = "s3";
			
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region;
		
	@Getter @Setter
	private Integer maxErrorRetry = -1;

	@Getter(lazy = true)
	private final AmazonS3 client = initClient();

	public AmazonS3PluginExtension(Project project) {
		this.project = project;
	}

	private AmazonS3 initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);

		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setMaxErrorRetry(maxErrorRetry);

		AmazonS3Client client = aws.createClient(AmazonS3Client.class, profileName, clientConfiguration);
		if (region != null) {
			client.setRegion(RegionUtils.getRegion(region));
		}
		return client;
	}
}
