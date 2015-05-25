package jp.classmethod.aws.gradle.s3;

import jp.classmethod.aws.gradle.AwsPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class AmazonS3Plugin implements Plugin<Project> {
	
	public void apply(Project project) {
		project.getPluginManager().apply(AwsPlugin.class);
		project.getExtensions().create(AmazonS3PluginExtension.NAME, AmazonS3PluginExtension.class, project);
	}
}
