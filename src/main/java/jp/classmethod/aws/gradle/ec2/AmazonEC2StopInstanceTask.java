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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonEC2StopInstanceTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private List<String> instanceIds = new ArrayList<>();
	
	@Getter
	private StopInstancesResult stopInstancesResult;
	
	
	public AmazonEC2StopInstanceTask() {
		super("AWS", "Stop EC2 instance.");
	}
	
	@TaskAction
	public void stopInstance() {
		// to enable conventionMappings feature
		List<String> instanceIds = getInstanceIds();
		
		if (instanceIds.isEmpty()) {
			return;
		}
		
		AmazonEC2PluginExtension ext = getPluginExtension(AmazonEC2PluginExtension.class);
		AmazonEC2 ec2 = ext.getClient();
		
		stopInstancesResult = ec2.stopInstances(new StopInstancesRequest(instanceIds));
		getLogger().info("Stop EC2 instance {} requested", instanceIds);
	}
}
