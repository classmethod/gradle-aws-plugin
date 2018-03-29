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
package jp.classmethod.aws.gradle.lambda;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;

import com.amazonaws.services.lambda.model.VpcConfig;

/**
 * <p>
 * wrapper around {@link com.amazonaws.services.lambda.model.VpcConfig}
 * </p>
 */
public class VpcConfigWrapper {
	
	@Getter
	@Setter
	private Collection<String> subnetIds;
	
	@Getter
	@Setter
	private Collection<String> securityGroupIds;
	
	
	/**
	 * Validates that at least one subnet and one security group are provided.
	 * @throws GradleException if at least one subnet and one security group are not set
	 */
	public void validate() {
		boolean missingSubnet = subnetIds == null || subnetIds.isEmpty();
		boolean missingSecurityGroup = securityGroupIds == null || securityGroupIds.isEmpty();
		if (missingSubnet || missingSecurityGroup) {
			throw new GradleException("At least one subnet ID and one security group are required for a VpcConfig");
		}
	}
	
	/**
	 * @return {@link VpcConfig} instance
	 * @throws GradleException if at least one subnet and one security group are not set
	 */
	public VpcConfig toVpcConfig() {
		this.validate();
		return new VpcConfig().withSubnetIds(this.subnetIds).withSecurityGroupIds(this.securityGroupIds);
	}
}
