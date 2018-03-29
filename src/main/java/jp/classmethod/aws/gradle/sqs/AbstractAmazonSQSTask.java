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
package jp.classmethod.aws.gradle.sqs;

import lombok.Setter;

import org.gradle.api.GradleException;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AbstractAmazonSQSTask extends BaseAwsTask {
	
	/**
	 * Amazon SQS queue URL
	 */
	@Setter
	private String queueUrl;
	
	/**
	 * Amazon SQS queue name, queueUrl takes precedence if also provided.
	 */
	@Setter
	private String queueName;
	
	
	public AbstractAmazonSQSTask(String group, String description) {
		super(group, description);
	}
	
	public String getQueueUrl() {
		if (queueUrl == null) {
			AmazonSQSPluginExtension ext = getPluginExtension(AmazonSQSPluginExtension.class);
			queueUrl = ext.getClient().getQueueUrl(queueName).getQueueUrl();
			if (queueUrl == null) {
				throw new GradleException("Unable to get queue url for queueName " + queueName);
			}
		}
		
		return queueUrl;
	}
}
