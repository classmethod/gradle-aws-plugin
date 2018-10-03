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
package jp.classmethod.aws.gradle.cloudwatch;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;

public class AwsCloudWatchListMetricsTask extends ConventionTask { // NOPMD
	
	@Getter
	@Setter
	private String metricName;
	
	@Getter
	@Setter
	private String namespace;
	
	@Getter
	private List<Metric> metrics;
	
	
	public AwsCloudWatchListMetricsTask() {
		setDescription("List CloudWatch metrics.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void listMetrics() { // NOPMD
		AwsCloudWatchPluginExtension ext = getProject().getExtensions().getByType(AwsCloudWatchPluginExtension.class);
		AmazonCloudWatch cw = ext.getClient();
		try {
			ListMetricsRequest request = new ListMetricsRequest()
				.withMetricName(getMetricName())
				.withNamespace(getNamespace());
			
			boolean done = false;
			
			while (!done) {
				ListMetricsResult response = cw.listMetrics(request);
				if (getMetrics() == null) {
					metrics = response.getMetrics();
				} else {
					metrics.addAll(response.getMetrics());
				}
				
				for (Metric metric : response.getMetrics()) {
					getLogger().debug("Retrieved metric %s", metric.getMetricName());
				}
				
				request.setNextToken(response.getNextToken());
				
				if (response.getNextToken() == null) {
					done = true;
				}
			}
		} catch (AmazonServiceException e) {
			throw new GradleException("Fail to describe metric: " + getMetricName(), e);
		}
	}
}
