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
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.RebootDBInstanceRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRDSRebootDBInstanceTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String dbInstanceIdentifier;
	
	@Getter
	@Setter
	private Boolean forceFailover;
	
	@Getter
	private DBInstance dbInstance;
	
	
	public AmazonRDSRebootDBInstanceTask() {
		super("AWS", "Reboot RDS instance.");
	}
	
	@TaskAction
	public void rebootDBInstance() {
		// to enable conventionMappings feature
		String dbInstanceIdentifier = getDbInstanceIdentifier();
		
		if (dbInstanceIdentifier == null) {
			throw new GradleException("dbInstanceIdentifier is required");
		}
		
		AmazonRDSPluginExtension ext = getPluginExtension(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		RebootDBInstanceRequest request = new RebootDBInstanceRequest()
			.withDBInstanceIdentifier(dbInstanceIdentifier)
			.withForceFailover(getForceFailover());
		dbInstance = rds.rebootDBInstance(request);
		getLogger().info("Reboot RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
	}
}
