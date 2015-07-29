/*
 * Copyright 2013-2015 Classmethod, Inc.
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

import groovy.lang.Closure;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Named;


public class EbAppVersionExtension implements Named {
	
	@Setter
	private Object label;
	
	@Getter @Setter
	private String description = "";
	
	@Getter @Setter
	private String bucket;
	
	@Setter
	private Object key;
	
	@Getter @Setter
	private File file;
	
	public String getLabel() {
		if (label instanceof Closure) {
			return ((Closure<?>)label).call().toString();
		}
		return label.toString();
	}
	
	String getKey() {
		if (key instanceof Closure) {
			return ((Closure<?>) key).call().toString();
		}
		return key.toString();
	}

	@Override
	public String getName() {
		return getLabel();
	}
}
