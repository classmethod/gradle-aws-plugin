package jp.classmethod.aws.gradle.cloudformation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import jp.classmethod.aws.gradle.AwsPlugin;
import jp.classmethod.aws.gradle.s3.AmazonS3FileUploadTask;
import jp.classmethod.aws.gradle.s3.AmazonS3Plugin;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.amazonaws.services.cloudformation.model.Parameter;

public class AmazonCloudFormationPlugin implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(AwsPlugin.class);
		project.getPluginManager().apply(AmazonS3Plugin.class);
		project.getExtensions().create(AmazonCloudFormationPluginExtension.NAME, AmazonCloudFormationPluginExtension.class,
				project);
		applyTasks(project);
	}
	
	
	private void applyTasks(Project project) {
		AmazonCloudFormationPluginExtension cfnExt =
				project.getExtensions().findByType(AmazonCloudFormationPluginExtension.class);
		
		AmazonS3FileUploadTask awsCfnUploadTemplate =
				project.getTasks().create("awsCfnUploadTemplate", AmazonS3FileUploadTask.class, task -> {
					task.setDescription("Upload cfn template file to the Amazon S3 bucket.");
					task.conventionMapping("file", () -> cfnExt.getTemplateFile());
					task.conventionMapping("bucketName", () -> cfnExt.getTemplateBucket());
					task.conventionMapping("key", () -> {
						String name = cfnExt.getTemplateFile().getName();
						return createKey(name, project.getVersion(), cfnExt.getTemplateKeyPrefix());
					});
					task.doLast(t -> {
						cfnExt.setTemplateURL(((AmazonS3FileUploadTask) t).getResourceUrl());
					});
				});
		
		AmazonCloudFormationMigrateStackTask awsCfnMigrateStack = project.getTasks()
				.create("awsCfnMigrateStack", AmazonCloudFormationMigrateStackTask.class, task -> {
					task.setDescription("Create/Migrate cfn stack.");
					task.mustRunAfter(awsCfnUploadTemplate);
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
					task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
					task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
							.map(it -> new Parameter()
									.withParameterKey(it.getKey().toString())
									.withParameterValue(it.getValue().toString()))
							.collect(Collectors.toList()));
					task.conventionMapping("cfnTemplateUrl", () -> cfnExt.getTemplateURL());
				});
		
		project.getTasks().create("awsCfnWaitStackReady", AmazonCloudFormationWaitStackStatusTask.class, task -> {
			task.setDescription("Wait cfn stack for *_COMPLETE status.");
			task.mustRunAfter(awsCfnMigrateStack);
			task.conventionMapping("stackName", () -> cfnExt.getStackName());
		});
		
		AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackComplete =
				project.getTasks().create("awsCfnWaitStackComplete", AmazonCloudFormationWaitStackStatusTask.class,
						task -> {
							task.setDescription("Wait cfn stack for CREATE_COMPETE or UPDATE_COMPLETE status.");
							task.mustRunAfter(awsCfnMigrateStack);
							task.setSuccessStatuses(Arrays.asList("CREATE_COMPLETE", "UPDATE_COMPLETE"));
							task.conventionMapping("stackName", () -> cfnExt.getStackName());
						});
		
		project.getTasks().create("awsCfnMigrateStackAndWaitCompleted")
			.dependsOn(awsCfnMigrateStack, awsCfnWaitStackComplete)
			.setDescription("Create/Migrate cfn stack, and wait stack for CREATE_COMPETE or UPDATE_COMPLETE status.");
		
		AmazonCloudFormationDeleteStackTask awsCfnDeleteStack =
				project.getTasks().create("awsCfnDeleteStack", AmazonCloudFormationDeleteStackTask.class, task -> {
					task.setDescription("Delete cfn stack.");
					task.conventionMapping("stackName", () -> cfnExt.getStackName());
				});
		
		AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackDeleted =
				project.getTasks().create("awsCfnWaitStackDeleted", AmazonCloudFormationWaitStackStatusTask.class,
						task -> {
							task.setDescription("Wait cfn stack for DELETE_COMPLETE status.");
							task.mustRunAfter(awsCfnDeleteStack);
							task.setSuccessStatuses(Arrays.asList("DELETE_COMPLETE"));
							task.conventionMapping("stackName", () -> cfnExt.getStackName());
						});
		
		project.getTasks().create("awsCfnDeleteStackAndWaitCompleted")
			.dependsOn(awsCfnDeleteStack, awsCfnWaitStackDeleted)
			.setDescription("Delete cfn stack, and wait stack for DELETE_COMPLETE status.");
	}


	private String createKey(String name, Object version, String prefix) {
		String path = name.substring(FilenameUtils.getPrefix(name).length());
		String baseName = FilenameUtils.getBaseName(name);
		String extension = FilenameUtils.getExtension(name);
		return String.format("%s/%s/%s-%s-%s%s", new Object[] {
				prefix,
				path,
				baseName,
				version,
				createTimestamp(),
				extension.length() > 0 ? "." + extension : ""
			});
	}


	private String createTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'_'HHmmss");
		sdf.setTimeZone(TimeZone.getDefault());
		String timestamp = sdf.format(new Date());
		return timestamp;
	}
}
