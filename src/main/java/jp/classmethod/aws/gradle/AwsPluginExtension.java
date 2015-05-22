package jp.classmethod.aws.gradle;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.google.common.base.Strings;


public class AwsPluginExtension {
	
	public static final String NAME = "aws";
	
	@Getter @Setter
	private Project project;
	
	@Getter @Setter
	private String profileName = "default";
	
	@Getter @Setter
	private String region = Regions.US_EAST_1.getName();
	
	
	public AwsPluginExtension(Project project) {
		this.project = project;
	}

	
	public AWSCredentialsProvider newCredentialsProvider(String profileName) {
		return new AWSCredentialsProviderChain(
			new EnvironmentVariableCredentialsProvider(),
			new SystemPropertiesCredentialsProvider(),
			Strings.isNullOrEmpty(profileName) == false ? new ProfileCredentialsProvider(profileName)
			: new AWSCredentialsProvider() {
				public void refresh() {}
				public AWSCredentials getCredentials() { return null; }
			},
			new ProfileCredentialsProvider(this.profileName),
			new InstanceProfileCredentialsProvider()
		);
	}
	
	public  <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, Region region, String profileName) {
		if (region == null) {
			if (this.region == null) {
				throw new IllegalStateException("default region is null");
			}
			region = RegionUtils.getRegion(this.region);
		}
		if (profileName == null) {
			if (this.profileName == null) {
				throw new IllegalStateException("default profileName is null");
			}
			profileName = this.profileName;
		}

		AWSCredentialsProvider credentialsProvider = newCredentialsProvider(profileName);
		return region.createClient(serviceClass, credentialsProvider, null);
	}
}
