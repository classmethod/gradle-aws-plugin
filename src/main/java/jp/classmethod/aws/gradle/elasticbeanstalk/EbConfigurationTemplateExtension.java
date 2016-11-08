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

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Named;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import groovy.lang.Closure;

public class EbConfigurationTemplateExtension implements Named {
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String desc;
	
	@Setter
	private Object optionSettings;
	
	@Getter
	@Setter
	private String solutionStackName;
	
	@Getter
	@Setter
	private boolean recreate = false;
	
	
	public EbConfigurationTemplateExtension(String name) {
		this.name = name;
	}
	
	public String getOptionSettings() throws IOException {
		if (optionSettings instanceof Closure) {
			Closure<?> closure = (Closure<?>) optionSettings;
			return closure.call().toString();
		}
		if (optionSettings instanceof File) {
			File file = (File) optionSettings;
			return Files.toString(file, Charsets.UTF_8);
		}
		return optionSettings.toString();
	}
}
