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
package jp.classmethod.aws.gradle.elasticloadbalancing;

import org.gradle.api.Project;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;

import jp.classmethod.aws.gradle.common.BasePluginExtension;

public class AmazonElasticLoadBalancingPluginExtension extends BasePluginExtension<AmazonElasticLoadBalancing> {
	
	public static final String NAME = "elb";
	
	
	public AmazonElasticLoadBalancingPluginExtension(Project project) {
		super(project, AmazonElasticLoadBalancingClientBuilder.standard());
	}
}
