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
