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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DeleteDBClusterRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRDSDeleteDBClusterTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String dbClusterIdentifier;
	
	@Getter
	@Setter
	private boolean skipFinalSnapshot;
	
	@Getter
	@Setter
	private String finalDBSnapshotIdentifier;
	
	@Getter
	private DBCluster dbCluster;
	
	
	public AmazonRDSDeleteDBClusterTask() {
		super("AWS", "Delete RDS cluster.");
	}
	
	@TaskAction
	public void deleteDBInstance() {
		String dbClusterIdentifier = getDbClusterIdentifier();
		
		if (dbClusterIdentifier == null) {
			throw new GradleException("dbClusterIdentifier is required");
		}
		
		AmazonRDSPluginExtension ext = getPluginExtension(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		try {
			DeleteDBClusterRequest request = new DeleteDBClusterRequest()
				.withDBClusterIdentifier(dbClusterIdentifier)
				.withSkipFinalSnapshot(isSkipFinalSnapshot())
				.withFinalDBSnapshotIdentifier(getFinalDBSnapshotIdentifier());
			dbCluster = rds.deleteDBCluster(request);
			getLogger().info("Delete RDS cluster requested: {}", dbCluster.getDBClusterIdentifier());
		} catch (DBInstanceNotFoundException e) {
			getLogger().warn(e.getMessage());
		}
	}
}
