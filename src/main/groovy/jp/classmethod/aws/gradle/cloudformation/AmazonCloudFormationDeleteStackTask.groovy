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
package jp.classmethod.aws.gradle.cloudformation

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.DeleteStackRequest
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction;


class AmazonCloudFormationDeleteStackTask extends DefaultTask {
	
	{
		description 'Delete cfn stack.'
		group = 'AWS'
	}
	
	def stackName
	
	@TaskAction
	def deleteStack() {
		if (! stackName) throw new GradleException("stackName is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn
		
		cfn.deleteStack(new DeleteStackRequest().withStackName(stackName))
		println "delete stack $stackName requested"
	}
}
