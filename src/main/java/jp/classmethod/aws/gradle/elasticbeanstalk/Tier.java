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
package jp.classmethod.aws.gradle.elasticbeanstalk;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier;

public enum Tier {
	
	WebServer(new EnvironmentTier()
		.withType("Standard")
		.withName("WebServer")
		.withVersion("1.0")),
	
	Worker(new EnvironmentTier()
		.withType("SQS/HTTP")
		.withName("Worker")
		.withVersion("1.0"));
	
	final EnvironmentTier environmentTier;
	
	
	Tier(EnvironmentTier environmentTier) {
		this.environmentTier = environmentTier;
	}
	
	public EnvironmentTier toEnvironmentTier() {
		return environmentTier;
	}
}
