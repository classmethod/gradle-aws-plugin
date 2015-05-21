package jp.classmethod.aws.gradle.s3;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;


public abstract class AbstractAmazonS3FileUploadTask extends ConventionTask {

	@Getter @Setter
	private String bucketName;

	@Getter @Setter
	private String key;

	@Getter @Setter
	private File file;

	@Getter @Setter
	private ObjectMetadata objectMetadata;

	@Getter @Setter
	private String resourceUrl;

	@Getter @Setter
	private boolean overwrite = false;

	protected ObjectMetadata existingObjectMetadata() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String key = getKey();

		AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getS3();

		try {
			// to enable conventionMapping, you must reference field via getters
			return s3.getObjectMetadata(bucketName, key);
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() != 404) {
				throw e;
			}
		}
		return null;
	}

	protected boolean exists() {
		return existingObjectMetadata() != null;
	}
}
