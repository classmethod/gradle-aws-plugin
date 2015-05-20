package jp.classmethod.aws.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class AwsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getExtensions().create(AwsPluginExtension.NAME, AwsPluginExtension.class, project);
	}
}
