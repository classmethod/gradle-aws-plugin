package jp.classmethod.aws.gradle.elasticbeanstalk;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Named;

import com.google.common.base.Charsets;

public class EbConfigurationTemplateExtension implements Named {
	
	static String readFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), Charsets.UTF_8);
	}
	
	
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
			return readFile(file);
		}
		return optionSettings.toString();
	}
}
