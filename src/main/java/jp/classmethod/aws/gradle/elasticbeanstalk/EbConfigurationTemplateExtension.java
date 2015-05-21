package jp.classmethod.aws.gradle.elasticbeanstalk;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Named;


public class EbConfigurationTemplateExtension implements Named {
	
	@Getter @Setter
	private String name;
	
	@Getter @Setter
	private String desc;
	
	@Setter
	private Object optionSettings;
	
	@Getter @Setter
	private String solutionStackName;
	
	@Getter @Setter
	private boolean recreate = false;
	
	public EbConfigurationTemplateExtension(String name) {
		this.name = name;
	}
	
	public String getOptionSettings() {
		if (optionSettings instanceof Closure) {
			return ((Closure<?>) optionSettings).call().toString();
		}
		return optionSettings.toString();
	}
}
