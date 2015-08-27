package jp.classmethod.aws.gradle.elasticbeanstalk;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.gradle.api.Project;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;

import jp.classmethod.aws.gradle.s3.AmazonS3FileUploadTask;
import lombok.Getter;
import lombok.Setter;


public class AWSElasticBeanstalkUploadBundleTask extends AmazonS3FileUploadTask {
	
	@Getter @Setter
	private String extension = "zip";
	
	@Getter
	private String versionLabel;
	
	
	public AWSElasticBeanstalkUploadBundleTask() {
		setDescription("Upload Elastic Beanstalk application bundle file to S3.");
	}
	
	@Override
	public void upload() throws IOException {
		Project project = getProject();
		AwsBeanstalkPluginExtension ext = project.getExtensions().getByType(AwsBeanstalkPluginExtension.class);
		AWSElasticBeanstalk eb = ext.getClient();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'_'HHmmss");
		df.setTimeZone(TimeZone.getDefault());
		versionLabel = String.format("%s-%s", project.getVersion().toString(), df.format(new Date()));
		
		String artifactId = project.property("artifactId").toString();
		
		setBucketName(eb.createStorageLocation().getS3Bucket());
		setKey(String.format("eb-apps/%s/%s-%s.%s", new Object[]{
				artifactId,
				artifactId,
				versionLabel,
				extension
		}));
		
		super.upload();
	}
}
