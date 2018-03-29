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

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterNotFoundException;
import com.amazonaws.services.rds.model.DescribeDBClustersRequest;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRDSWaitClusterStatusTask extends BaseAwsTask { // NOPMD
	
	@Getter
	@Setter
	private String dbClusterIdentifier;
	
	@Getter
	@Setter
	private List<String> successStatuses = Arrays.asList(
			"available",
			"backing-up",
			"terminated");
	
	@Getter
	@Setter
	private List<String> waitStatuses = Arrays.asList(
			"creating",
			"deleting",
			"modifying",
			"rebooting",
			"renaming",
			"resetting-master-credentials");
	
	@Getter
	@Setter
	private int loopTimeout = 900; // sec
	
	@Getter
	@Setter
	private int loopWait = 10; // sec
	
	@Getter
	private boolean found;
	
	@Getter
	private String lastStatus;
	
	
	public AmazonRDSWaitClusterStatusTask() {
		super("AWS", "Wait RDS cluster for specific status.");
	}
	
	@TaskAction
	public void waitClusterForStatus() { // NOPMD
		// to enable conventionMappings feature
		String dbClusterIdentifier = getDbClusterIdentifier();
		List<String> successStatuses = getSuccessStatuses();
		List<String> waitStatuses = getWaitStatuses();
		int loopTimeout = getLoopTimeout();
		int loopWait = getLoopWait();
		
		if (dbClusterIdentifier == null) {
			throw new GradleException("dbClusterIdentifier is not specified");
		}
		
		AmazonRDSPluginExtension ext = getPluginExtension(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		long start = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
				throw new GradleException("Timeout");
			}
			try {
				DescribeDBClustersResult dir = rds.describeDBClusters(new DescribeDBClustersRequest()
					.withDBClusterIdentifier(dbClusterIdentifier));
				DBCluster dbCluster = dir.getDBClusters().get(0);
				
				found = true;
				lastStatus = dbCluster.getStatus();
				if (successStatuses.contains(lastStatus)) {
					getLogger().info("Status of DB cluster {} is now {}.", dbClusterIdentifier, lastStatus);
					break;
				} else if (waitStatuses.contains(lastStatus)) {
					getLogger().info("Status of DB cluster {} is {}...", dbClusterIdentifier, lastStatus);
					try {
						Thread.sleep(loopWait * 1000);
					} catch (InterruptedException e) {
						throw new GradleException("Sleep interrupted", e);
					}
				} else {
					// fail when current status is not waitStatuses or successStatuses
					throw new GradleException(
							"Status of " + dbClusterIdentifier + " is " + lastStatus + ".  It seems to be failed.");
				}
			} catch (DBClusterNotFoundException e) {
				throw new GradleException(dbClusterIdentifier + " does not exist", e);
			} catch (AmazonServiceException e) {
				if (found) {
					break;
				} else {
					throw new GradleException("Fail to describe instance: " + dbClusterIdentifier, e);
				}
			}
		}
	}
}
