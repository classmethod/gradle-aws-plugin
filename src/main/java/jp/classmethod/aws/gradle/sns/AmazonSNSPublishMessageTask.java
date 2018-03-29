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
package jp.classmethod.aws.gradle.sns;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonSNSPublishMessageTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String topicArn;
	
	@Getter
	@Setter
	private String message;
	
	@Getter
	@Setter
	private String subject;
	
	@Getter
	@Setter
	private String messageStructure;
	
	
	public AmazonSNSPublishMessageTask() {
		super("AWS", "Publish message to SNS");
	}
	
	@TaskAction
	public void publishMessage() {
		String topicArn = getTopicArn();
		String message = getMessage();
		String subject = getSubject();
		String messageStructure = getMessageStructure();
		
		if (topicArn == null) {
			throw new GradleException("Must specify SNS topicArn");
		}
		if (message == null) {
			throw new GradleException("Must provide message to send to SNS");
		}
		
		AmazonSNSPluginExtension ext = getPluginExtension(AmazonSNSPluginExtension.class);
		AmazonSNS sns = ext.getClient();
		
		PublishRequest request = new PublishRequest().withTopicArn(topicArn).withMessage(message);
		if (subject != null) {
			request.setSubject(subject);
		}
		if (messageStructure != null) {
			request.setMessageStructure(messageStructure);
		}
		sns.publish(request);
	}
}
