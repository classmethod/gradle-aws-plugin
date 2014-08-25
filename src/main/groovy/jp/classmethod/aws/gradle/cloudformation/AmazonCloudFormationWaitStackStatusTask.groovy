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

import com.amazonaws.*
import com.amazonaws.services.cloudformation.*
import com.amazonaws.services.cloudformation.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class AmazonCloudFormationWaitStackStatusTask extends DefaultTask {
	
	{
		description 'Wait cfn stack for specific status.'
		group = 'AWS'
	}
	
	String stackName
	
	List<String> successStatuses = [
		'CREATE_COMPLETE',
		'UPDATE_COMPLETE',
		'ROLLBACK_COMPLETE',
		'UPDATE_ROLLBACK_COMPLETE',
		'DELETE_COMPLETE',
	]

	List<String> waitStatuses = [
		'CREATE_IN_PROGRESS',
		'ROLLBACK_IN_PROGRESS',
		'DELETE_IN_PROGRESS',
		'UPDATE_IN_PROGRESS',
		'UPDATE_COMPLETE_CLEANUP_IN_PROGRESS',
		'UPDATE_ROLLBACK_IN_PROGRESS',
		'UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS'
	]
	
	int loopTimeout = 900 // sec
	int loopWait = 10 // sec
	
	boolean found
	
	String lastStatus
	
	@TaskAction
	def waitStackForStatus() {
		if (! getStackName()) throw new GradleException("stackName is not specified")
		
		AwsCloudFormationPluginExtension ext = project.extensions.getByType(AwsCloudFormationPluginExtension)
		AmazonCloudFormation cfn = ext.cfn

		def start = System.currentTimeMillis()
		while (true) {
			if (System.currentTimeMillis() > start + (getLoopTimeout() * 1000)) {
				throw new GradleException('Timeout')
			}
			try {
				def describeStackResult = cfn.describeStacks(new DescribeStacksRequest().withStackName(getStackName()))
				Stack stack = describeStackResult.stacks[0]
				if (stack == null) {
					throw new GradleException("stack ${getStackName()} is not exists")
				}
				found = true
				lastStatus = stack.stackStatus
				if (getSuccessStatuses().contains(lastStatus)) {
					println "Status of stack ${getStackName()} is now ${lastStatus}."
					break
				} else if (getWaitStatuses().contains(lastStatus)) {
					println "Status of stack ${getStackName()} is ${lastStatus}..."
					Thread.sleep(getLoopWait() * 1000)
				} else {
					// waitStatusesでもsuccessStatusesないステータスはfailとする
					throw new GradleException("Status of stack ${getStackName()} is ${lastStatus}.  It seems to be failed.")
				}
			} catch (AmazonServiceException e) {
				if (found) {
					break
				} else {
					throw new GradleException("Fail to describe stack: ${getStackName()}", e)
				}
			}
		}
	}
}
