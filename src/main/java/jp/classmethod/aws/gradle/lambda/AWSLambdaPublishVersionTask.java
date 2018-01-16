package jp.classmethod.aws.gradle.lambda;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.PublishVersionRequest;
import com.amazonaws.services.lambda.model.PublishVersionResult;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by frankfarrell on 16/01/2018.
 *
 * https://docs.aws.amazon.com/cli/latest/reference/lambda/publish-version.html
 */
public class AWSLambdaPublishVersionTask extends ConventionTask {

    /*
    The function name for which the alias is created.
    Note that the length constraint applies only to the ARN.
    If you specify only the function name, it is limited to 64 characters in length.
     */
    @Getter
    @Setter
    private String functionName;

    @Getter
    @Setter
    private String codeSha256;

    @Getter
    @Setter
    private String description;

    @Getter
    private PublishVersionResult publishVersionResult;

    @TaskAction
    public void publishVersion(){

        final String functionName = getFunctionName();

        if (functionName == null) {
            throw new GradleException("functionName is required");
        }

        final AWSLambda lambda = getAwsLambdaClient();

        PublishVersionRequest request = new PublishVersionRequest().withFunctionName(functionName);

        if(getCodeSha256() != null){
            request.withCodeSha256(getCodeSha256());
        }
        if(getDescription() != null){
            request.withDescription(getDescription());
        }

        publishVersionResult = lambda.publishVersion(request);

        getLogger().info("Publish lambda version for {} succeded with version {}", functionName, publishVersionResult.getVersion());
    }

    private AWSLambda getAwsLambdaClient() {
        final AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
        return ext.getClient();
    }
}