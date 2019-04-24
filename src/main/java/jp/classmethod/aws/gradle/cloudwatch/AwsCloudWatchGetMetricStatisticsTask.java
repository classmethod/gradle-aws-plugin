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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class AwsCloudWatchGetMetricStatisticsTask extends ConventionTask { // NOPMD
	
	@Getter
	@Setter
	private String metricName;
	
	@Getter
	@Setter
	private String namespace;
	
	@Getter
	@Setter
	private Integer period;
	
	@Getter
	@Setter
	private String statistics;
	
	@Getter
	@Setter
	private String extendedStatistics;
	
	@Getter
	@Setter
	private Map<String, String> dimensions;
	
	@Getter
	@Setter
	private Date startTime;
	
	@Getter
	@Setter
	private Date endTime;
	
	@Getter
	@Setter
	private String unit;
	
	@Getter
	private List<Datapoint> datapoints;
	
	@Getter
	private String label;
	
	
	public AwsCloudWatchGetMetricStatisticsTask() {
		setDescription("List CloudWatch metric statistics.");
		setGroup("AWS");
	}
	
	public GetMetricStatisticsResult getMetricStatistics(String metricName, String namespace, String statistics,
			String extendedStatistics, String unit) throws AmazonServiceException {
		AwsCloudWatchPluginExtension ext = getProject().getExtensions().getByType(AwsCloudWatchPluginExtension.class);
		AmazonCloudWatch cw = ext.getClient();
		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
			.withMetricName(metricName)
			.withNamespace(namespace)
			.withStatistics(statistics)
			.withExtendedStatistics(extendedStatistics)
			.withUnit(unit)
			.withEndTime(getEndTime())
			.withStartTime(getStartTime())
			.withPeriod(getPeriod());
		
		if (getDimensions() != null) {
			request.withDimensions(getDimensions().entrySet().stream()
				.map(it -> new Dimension()
					.withName(it.getKey().toString())
					.withValue(it.getValue()))
				.collect(Collectors.toList()));
		}
		
		GetMetricStatisticsResult response = cw.getMetricStatistics(request);
		for (Datapoint metric : response.getDatapoints()) {
			getLogger().debug("Retrieved metric %s", metric.getAverage());
		}
		return response;
	}
	
	@TaskAction
	public void getMetricStatistics() { // NOPMD
		try {
			GetMetricStatisticsResult result = getMetricStatistics(getMetricName(), getNamespace(), getStatistics(),
					getExtendedStatistics(), getUnit());
			datapoints = result.getDatapoints();
			label = result.getLabel();
		} catch (AmazonServiceException e) {
			throw new GradleException("Fail to describe metric: " + getMetricName(), e);
		}
	}
}
