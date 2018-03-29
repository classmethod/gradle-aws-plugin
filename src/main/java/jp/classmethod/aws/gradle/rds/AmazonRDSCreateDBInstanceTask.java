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

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Tag;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRDSCreateDBInstanceTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String dbName;
	
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
	private String engine;
	
	@Getter
	@Setter
	private String masterUsername;
	
	@Getter
	@Setter
	private String masterUserPassword;
	
	@Getter
	@Setter
	private List<String> vpcSecurityGroupIds;
	
	@Getter
	@Setter
	private String dbSubnetGroupName;
	
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
	private Integer port;
	
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
	private String licenseModel;
	
	@Getter
	@Setter
	private Integer iops;
	
	@Getter
	@Setter
	private String optionGroupName;
	
	@Getter
	@Setter
	private Boolean publiclyAccessible;
	
	@Getter
	@Setter
	private String characterSetName;
	
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
	@Setter
	private Boolean storageEncrypted;
	
	@Getter
	@Setter
	private String kmsKeyId;
	
	@Getter
	@Setter
	private Boolean copyTagsToSnapshot;
	
	@Getter
	@Setter
	private Integer promotionTier;
	
	@Getter
	@Setter
	private String dbClusterIdentifier;
	
	@Getter
	@Setter
	private String availabilityZone;
	
	@Getter
	@Setter
	private List<String> securityGroups;
	
	@Getter
	private DBInstance dbInstance;
	
	@Getter
	@Setter
	private Map<String, String> tags;
	
	
	public AmazonRDSCreateDBInstanceTask() {
		super("AWS", "Create RDS instance.");
	}
	
	@TaskAction
	public void createDBInstance() {
		// to enable conventionMappings feature
		String dbInstanceIdentifier = getDbInstanceIdentifier();
		String dbInstanceClass = getDbInstanceClass();
		String engine = getEngine();
		
		if (dbInstanceClass == null) {
			throw new GradleException("dbInstanceClass is required");
		}
		if (dbInstanceIdentifier == null) {
			throw new GradleException("dbInstanceIdentifier is required");
		}
		if (engine == null) {
			throw new GradleException("engine is required");
		}
		
		AmazonRDSPluginExtension ext = getPluginExtension(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		CreateDBInstanceRequest request = new CreateDBInstanceRequest()
			.withDBName(getDbName())
			.withDBInstanceIdentifier(dbInstanceIdentifier)
			.withAllocatedStorage(getAllocatedStorage())
			.withDBInstanceClass(dbInstanceClass)
			.withEngine(engine)
			.withMasterUsername(getMasterUsername())
			.withMasterUserPassword(getMasterUserPassword())
			.withVpcSecurityGroupIds(getVpcSecurityGroupIds())
			.withDBSecurityGroups(getSecurityGroups())
			.withDBSubnetGroupName(getDbSubnetGroupName())
			.withPreferredMaintenanceWindow(getPreferredMaintenanceWindow())
			.withDBParameterGroupName(getDbParameterGroupName())
			.withBackupRetentionPeriod(getBackupRetentionPeriod())
			.withPreferredBackupWindow(getPreferredBackupWindow())
			.withPort(getPort())
			.withMultiAZ(getMultiAZ())
			.withEngineVersion(getEngineVersion())
			.withAutoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
			.withLicenseModel(getLicenseModel())
			.withIops(getIops())
			.withOptionGroupName(getOptionGroupName())
			.withPubliclyAccessible(getPubliclyAccessible())
			.withCharacterSetName(getCharacterSetName())
			.withStorageType(getStorageType())
			.withTdeCredentialArn(getTdeCredentialArn())
			.withTdeCredentialPassword(getTdeCredentialPassword())
			.withStorageEncrypted(getStorageEncrypted())
			.withKmsKeyId(getKmsKeyId())
			.withCopyTagsToSnapshot(getCopyTagsToSnapshot())
			.withPromotionTier(getPromotionTier())
			.withDBClusterIdentifier(getDbClusterIdentifier())
			.withAvailabilityZone(getAvailabilityZone())
			.withTags(getTags().entrySet().stream()
				.map(it -> new Tag()
					.withKey(it.getKey().toString())
					.withValue(it.getValue().toString()))
				.collect(Collectors.toList()));
		
		dbInstance = rds.createDBInstance(request);
		getLogger().info("Create RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
	}
}
