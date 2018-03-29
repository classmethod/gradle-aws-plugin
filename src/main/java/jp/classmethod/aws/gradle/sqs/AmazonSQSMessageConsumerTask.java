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
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class AmazonSQSMessageConsumerTask extends AbstractAmazonSQSTask {
	
	private static final int MAX_MESSAGE_CONSUME_BATCH_SIZE = 10;
	
	@Setter
	private boolean showMessages = true;
	
	@Setter
	private boolean deleteMessages = true;
	
	@Setter
	private int maxNumberOfMessages = 1000;
	
	
	public AmazonSQSMessageConsumerTask() {
		super("AWS", "Consume/Delete SQS messages");
	}
	
	@TaskAction
	public void consumeMessages() {
		String queueUrl = getQueueUrl();
		
		if (queueUrl == null) {
			throw new GradleException("Must specify either queueName or queueUrl");
		}
		
		AmazonSQSPluginExtension ext = getPluginExtension(AmazonSQSPluginExtension.class);
		AmazonSQS sqs = ext.getClient();
		
		int counter = 0;
		while (counter < maxNumberOfMessages) {
			ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(queueUrl)
				.withMaxNumberOfMessages(MAX_MESSAGE_CONSUME_BATCH_SIZE).withVisibilityTimeout(30);
			List<DeleteMessageBatchRequestEntry> messagesToDelete =
					sqs.receiveMessage(request).getMessages().stream().map(message -> {
						if (showMessages) {
							getLogger().lifecycle(String.format(Locale.ENGLISH,
									"Read message id: %s, message body: %200s",
									message.getMessageId(), message.getBody()));
						}
						return new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle());
					}).collect(Collectors.toList());
			
			if (messagesToDelete.isEmpty()) {
				break;
			}
			
			deleteMessages(sqs, queueUrl, messagesToDelete);
			counter += messagesToDelete.size();
		}
		
		getLogger().lifecycle("Consumed a total of {} messages from {}", counter, queueUrl);
	}
	
	private void deleteMessages(AmazonSQS sqs, String queueUrl, List<DeleteMessageBatchRequestEntry> messagesToDelete) {
		if (!deleteMessages || messagesToDelete.isEmpty()) {
			return;
		}
		
		getLogger().lifecycle("Deleting {} messages from {}, still working...", messagesToDelete.size(), queueUrl);
		sqs.deleteMessageBatch(queueUrl, messagesToDelete);
	}
	
}
