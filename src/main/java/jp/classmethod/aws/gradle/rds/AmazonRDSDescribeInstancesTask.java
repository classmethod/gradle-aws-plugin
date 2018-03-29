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
package jp.classmethod.aws.gradle.rds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.Filter;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRDSDescribeInstancesTask extends BaseAwsTask { // NOPMD
	
	@Getter
	@Setter
	private String dbInstanceIdentifier;
	
	@Getter
	@Setter
	private int maxRecords = 50;
	
	@Getter
	@Setter
	private Map<String, List<String>> filters;
	
	@Getter
	private List<DBInstance> dbInstances;
	
	
	public AmazonRDSDescribeInstancesTask() {
		super("AWS", "Describe AWS instances.");
	}
	
	@TaskAction
	public void describeDBInstances() { // NOPMD
		// to enable conventionMappings feature
		String dbInstanceIdentifier = getDbInstanceIdentifier();
		AmazonRDSPluginExtension ext = getPluginExtension(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		try {
			DescribeDBInstancesRequest request = new DescribeDBInstancesRequest()
				.withMaxRecords(getMaxRecords());
			if (getDbInstanceIdentifier() != null && getDbInstanceIdentifier().length() > 0) {
				request.withDBInstanceIdentifier(getDbInstanceIdentifier());
			}
			if (getFilters() != null) {
				request.withFilters(getFilters().entrySet().stream()
					.map(it -> new Filter()
						.withName(it.getKey().toString())
						.withValues(it.getValue()))
					.collect(Collectors.toList()));
			}
			DescribeDBInstancesResult dir = rds.describeDBInstances(request);
			dbInstances = dir.getDBInstances();
		} catch (AmazonServiceException e) {
			throw new GradleException("Fail to describe instance: " + dbInstanceIdentifier, e);
		}
	}
}
