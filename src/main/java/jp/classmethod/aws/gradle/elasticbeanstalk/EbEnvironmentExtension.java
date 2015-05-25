package jp.classmethod.aws.gradle.elasticbeanstalk;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;

import org.gradle.util.Configurable;

public class EbEnvironmentExtension implements Configurable<Void> {
	
	@Getter @Setter
	private String envName;
	
	@Getter @Setter
	private String envDesc = "";
	
	@Getter @Setter
	private String cnamePrefix;
	
	@Getter @Setter
	private String templateName;
	
	@Getter @Setter
	private String versionLabel;
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public Void configure(Closure closure) {
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(this);
		closure.call();
		return null;
	}
}
