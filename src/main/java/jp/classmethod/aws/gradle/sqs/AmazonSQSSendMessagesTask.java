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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.google.common.collect.Lists;

public class AmazonSQSSendMessagesTask extends AbstractAmazonSQSTask {
	
	private static final int MAX_MESSAGE_SEND_BATCH_SIZE = 10;
	
	@Getter
	@Setter
	private Stream<String> messages;
	
	
	public AmazonSQSSendMessagesTask() {
		super("AWS", "Send messages to SQS");
	}
	
	@TaskAction
	public void sendMessages() {
		String queueUrl = getQueueUrl();
		Stream<String> messages = getMessages();
		
		if (queueUrl == null) {
			throw new GradleException("Must specify either queueName or queueUrl");
		}
		if (messages == null) {
			throw new GradleException("Must provide messages to send to SQS");
		}
		
		AmazonSQSPluginExtension ext = getPluginExtension(AmazonSQSPluginExtension.class);
		AmazonSQS sqs = ext.getClient();
		
		final AtomicInteger counter = new AtomicInteger(0);
		List<SendMessageBatchRequestEntry> messageEntries = messages.map(message -> new SendMessageBatchRequestEntry()
			.withId("gradle_message_index_" + counter.getAndIncrement()).withMessageBody(message))
			.collect(Collectors.toList());
		
		getLogger().info("Sending {} messages to {}", messageEntries.size(), queueUrl);
		Lists.partition(messageEntries, MAX_MESSAGE_SEND_BATCH_SIZE).parallelStream().forEach(messagesToSend -> sqs
			.sendMessageBatch(new SendMessageBatchRequest().withQueueUrl(queueUrl).withEntries(messagesToSend)));
	}
}
