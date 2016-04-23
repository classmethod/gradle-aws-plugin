package jp.classmethod.aws.gradle.common;

import com.amazonaws.AmazonWebServiceClient;
import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

public class BaseRegionAwarePluginExtension<T extends AmazonWebServiceClient> extends BasePluginExtension<T> {

    @Getter
    @Setter
    private String region;

    public BaseRegionAwarePluginExtension(Project project, Class<T> awsClientClass) {
        super(project, awsClientClass);
    }

    @Override
    protected T initClient() {
        AwsPluginExtension aws = getProject().getExtensions().getByType(AwsPluginExtension.class);
        T client = super.initClient();
        if(isRegionRequired() || region != null) {
            client.setRegion(aws.getActiveRegion(region));
        }

        return client;
    }

    /**
     * Most clients require a region to be set, but a few allow it to be optional.
     * For optional clients, subclasses should override and return false.
     *
     * @return true if region is required (default), else false.
     */
    protected boolean isRegionRequired() {
        return true;
    }

}
