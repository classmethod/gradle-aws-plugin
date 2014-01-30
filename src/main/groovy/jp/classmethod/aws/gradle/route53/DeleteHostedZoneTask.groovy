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

import com.amazonaws.services.route53.AmazonRoute53
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class DeleteHostedZoneTask extends DefaultTask {
	
	def String hostedZoneId
	
	@TaskAction
	def createHostedZone() {
		AmazonRoute53PluginExtension ext = project.extensions.getByType(AmazonRoute53PluginExtension)
		AmazonRoute53 r53 = ext.r53
		
		r53.deleteHostedZone(new DeleteHostedZoneRequest(hostedZoneId))
	}
}
