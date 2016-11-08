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

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.gradle.util.Configurable;

import groovy.lang.Closure;

public class EbEnvironmentExtension implements Configurable<Void> {
	
	@Getter
	@Setter
	private String envName;
	
	@Getter
	@Setter
	private String envDesc = "";
	
	@Getter
	@Setter
	private String cnamePrefix;
	
	@Getter
	@Setter
	private String templateName;
	
	@Getter
	@Setter
	private String versionLabel;
	
	@Getter
	@Setter
	private Map<String, String> tags = new HashMap<String, String>();
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public Void configure(Closure closure) {
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(this);
		closure.call();
		return null;
	}
}
