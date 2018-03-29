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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.ModifyDBInstanceRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRDSModifyDBInstanceTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String dbInstanceIdentifier;
	
	@Getter
	@Setter
	private Integer allocatedStorage;
	
	@Getter
	@Setter
	private String dbInstanceClass;
	
	@Getter
	@Setter
	private String masterUserPassword;
	
	@Getter
	@Setter
	private List<String> vpcSecurityGroupIds;
	
	@Getter
	@Setter
	private String preferredMaintenanceWindow;
	
	@Getter
	@Setter
	private String dbParameterGroupName;
	
	@Getter
	@Setter
	private Integer backupRetentionPeriod;
	
	@Getter
	@Setter
	private String preferredBackupWindow;
	
	@Getter
	@Setter
	private Boolean multiAZ;
	
	@Getter
	@Setter
	private String engineVersion;
	
	@Getter
	@Setter
	private Boolean autoMinorVersionUpgrade;
	
	@Getter
	@Setter
	private Integer iops;
	
	@Getter
	@Setter
	private String optionGroupName;
	
	@Getter
	@Setter
	private String storageType;
	
	@Getter
	@Setter
	private String tdeCredentialArn;
	
	@Getter
	@Setter
	private String tdeCredentialPassword;
	
	@Getter
	private DBInstance dbInstance;
	
	
	public AmazonRDSModifyDBInstanceTask() {
		super("AWS", "Modify RDS instance.");
	}
	
	@TaskAction
	public void modifyDBInstance() {
		// to enable conventionMappings feature
		String dbInstanceIdentifier = getDbInstanceIdentifier();
		
		if (dbInstanceIdentifier == null) {
			throw new GradleException("dbInstanceIdentifier is required");
		}
		
		AmazonRDSPluginExtension ext = getPluginExtension(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		ModifyDBInstanceRequest request = new ModifyDBInstanceRequest()
			.withDBInstanceIdentifier(dbInstanceIdentifier)
			.withAllocatedStorage(getAllocatedStorage())
			.withDBInstanceClass(getDbInstanceClass())
			.withMasterUserPassword(getMasterUserPassword())
			.withVpcSecurityGroupIds(getVpcSecurityGroupIds())
			.withPreferredMaintenanceWindow(getPreferredMaintenanceWindow())
			.withDBParameterGroupName(getDbParameterGroupName())
			.withBackupRetentionPeriod(getBackupRetentionPeriod())
			.withPreferredBackupWindow(getPreferredBackupWindow())
			.withMultiAZ(getMultiAZ())
			.withEngineVersion(getEngineVersion())
			.withAutoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
			.withIops(getIops())
			.withOptionGroupName(getOptionGroupName())
			.withStorageType(getStorageType())
			.withTdeCredentialArn(getTdeCredentialArn())
			.withTdeCredentialPassword(getTdeCredentialPassword());
		dbInstance = rds.modifyDBInstance(request);
		getLogger().info("Modify RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
	}
}
