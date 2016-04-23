package jp.classmethod.aws.gradle.common;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

public abstract class BasePluginExtention<T extends AmazonWebServiceClient> {

    private final Class<T> awsClientClass;

    @Getter
    @Setter
    private Project project;

    @Getter @Setter
    private	String profileName;

    @Getter(lazy = true)
    private final T client = initClient();

    public BasePluginExtention(Project project, Class<T> awsClientClass) {
        this.project = project;
        this.awsClientClass = awsClientClass;
    }

    protected T initClient() {
        AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
        return aws.createClient(awsClientClass, profileName, buildClientConfiguration());
    }

    /**
     * Allow subclasses to build a custom client configuration.
     */
    protected ClientConfiguration buildClientConfiguration() {
        return null;
    }

}
