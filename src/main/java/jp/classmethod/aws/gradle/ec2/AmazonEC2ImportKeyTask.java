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
package jp.classmethod.aws.gradle.ec2;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.ImportKeyPairResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonEC2ImportKeyTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String keyName;
	
	@Getter
	@Setter
	private String publicKeyMaterial;
	
	@Getter
	@Setter
	public boolean ifNotExists;
	
	@Getter
	private ImportKeyPairResult importKeyPairResult;
	
	
	public AmazonEC2ImportKeyTask() {
		super("AWS", "Start EC2 instance.");
	}
	
	@TaskAction
	public void importKey() {
		// to enable conventionMappings feature
		String keyName = getKeyName();
		String publicKeyMaterial = getPublicKeyMaterial();
		
		if (keyName == null) {
			throw new GradleException("keyName is required");
		}
		
		AmazonEC2PluginExtension ext = getPluginExtension(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		if (isIfNotExists() == false || exists(ec2) == false) {
			importKeyPairResult = ec2.importKeyPair(new ImportKeyPairRequest(keyName, publicKeyMaterial));
			getLogger().info("KeyPair imported: {}", importKeyPairResult.getKeyFingerprint());
		} else {
			getLogger().info("KeyPair already exists: {}", keyName);
		}
	}
	
	private boolean exists(AmazonEC2 ec2) {
		// to enable conventionMappings feature
		String keyName = getKeyName();
		
		try {
			DescribeKeyPairsResult describeKeyPairsResult =
					ec2.describeKeyPairs(new DescribeKeyPairsRequest().withKeyNames(keyName));
			return describeKeyPairsResult.getKeyPairs().isEmpty() == false;
		} catch (AmazonClientException e) {
			return false;
		}
	}
}
