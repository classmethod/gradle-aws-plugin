/*
 * Copyright 2013-2014 Classmethod, Inc.
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
package jp.classmethod.aws.gradle.route53

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.amazonaws.*
import com.amazonaws.services.route53.*
import com.amazonaws.services.route53.model.*

class CreateHostedZoneTask extends DefaultTask {
	
	String hostedZoneName
	
	String callerReference
	
	String comment
	
	@TaskAction
	def createHostedZone() {
		AmazonRoute53PluginExtension ext = project.extensions.getByType(AmazonRoute53PluginExtension)
		AmazonRoute53 r53 = ext.r53
		
		String callerRef = callerReference == null ? InetAddress.getLocalHost().getHostName() : callerReference
		println "callerRef = ${callerRef}"
		
		CreateHostedZoneRequest req = new CreateHostedZoneRequest()
			.withName(hostedZoneName)
			.withCallerReference(callerRef)
		if (comment != null) {
			req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment))
		}
		
		try {
			CreateHostedZoneResult chzr = r53.createHostedZone(req)
			println "HostedZone ${hostedZoneName} - ${callerRef} is created."
			for (String nameServer in chzr.delegationSet.nameServers) {
				println "NS ${nameServer}"
			}
		} catch (HostedZoneAlreadyExistsException e) {
			println "HostedZone ${hostedZoneName} - ${callerRef} is already created."
		}
	}
}
