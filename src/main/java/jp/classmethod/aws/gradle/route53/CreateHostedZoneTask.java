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
package jp.classmethod.aws.gradle.route53;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZoneAlreadyExistsException;
import com.amazonaws.services.route53.model.HostedZoneConfig;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class CreateHostedZoneTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String hostedZoneName;
	
	@Getter
	@Setter
	private String callerReference;
	
	@Getter
	@Setter
	private String comment;
	
	// after did work
	
	@Getter
	private CreateHostedZoneResult createHostedZoneResult;
	
	@Getter
	private String hostedZoneId;
	
	@Getter
	private List<String> nameServers;
	
	
	public CreateHostedZoneTask() {
		super("AWS", "Create hosted zone.");
	}
	
	@TaskAction
	public void createHostedZone() throws UnknownHostException {
		// to enable conventionMappings feature
		String hostedZoneName = getHostedZoneName();
		String callerReference =
				getCallerReference() != null ? getCallerReference() : InetAddress.getLocalHost().getHostName();
		String comment = getComment();
		
		AmazonRoute53PluginExtension ext = getPluginExtension(AmazonRoute53PluginExtension.class);
		AmazonRoute53 route53 = ext.getClient();
		
		getLogger().info("callerRef = {}", callerReference);
		
		CreateHostedZoneRequest req = new CreateHostedZoneRequest()
			.withName(hostedZoneName)
			.withCallerReference(callerReference);
		if (comment != null) {
			req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
		}
		
		try {
			createHostedZoneResult = route53.createHostedZone(req);
			nameServers = createHostedZoneResult.getDelegationSet().getNameServers();
			hostedZoneId = createHostedZoneResult.getHostedZone().getId();
			getLogger().info("HostedZone {} ({} - {})  is created.", hostedZoneId, hostedZoneName, callerReference);
			nameServers.forEach(it -> {
				getLogger().info("  NS {}", it);
			});
		} catch (HostedZoneAlreadyExistsException e) {
			getLogger().error("HostedZone {} - {} is already created.", hostedZoneName, callerReference);
		}
	}
}
