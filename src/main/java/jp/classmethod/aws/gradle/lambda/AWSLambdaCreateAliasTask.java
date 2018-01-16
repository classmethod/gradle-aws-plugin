package jp.classmethod.aws.gradle.lambda;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AliasRoutingConfiguration;
import com.amazonaws.services.lambda.model.CreateAliasRequest;
import com.amazonaws.services.lambda.model.CreateAliasResult;
import com.amazonaws.services.lambda.model.UpdateAliasResult;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by frankfarrell on 16/01/2018.
 * https://docs.aws.amazon.com/cli/latest/reference/lambda/create-alias.html
 */
public class AWSLambdaCreateAliasTask  extends ConventionTask {

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
    private String name;

    /*
    Using this parameter you can change the Lambda function version to which the alias points.
    If you do not specify it, the alias will point by default to $LATEST
     */
    @Getter
    @Setter
    private String functionVersion;

    /*
    You can change the description of the alias using this parameter.
    */
    @Getter
    @Setter
    private String aliasDescription;
    /*
    https://docs.aws.amazon.com/lambda/latest/dg/lambda-traffic-shifting-using-aliases.html
     */
    @Getter
    @Setter
    private RoutingConfig routingConfig;

    @Getter
    private CreateAliasResult createAliasResult;

    @TaskAction
    public void createAlias() {
        final String functionName = getFunctionName();
        final String aliasName = getName();
        final String functionVersion = getFunctionVersion();

        if (functionName == null) {
            throw new GradleException("functionName is required");
        }
        if (aliasName == null) {
            throw new GradleException("name for alias is required");
        }
        if (functionVersion == null) {
            throw new GradleException("functionVersion for alias is required");
        }

        final CreateAliasRequest request = new CreateAliasRequest().withFunctionName(functionName)
                .withFunctionVersion(functionVersion)
                .withName(aliasName);

        final AWSLambda lambda = getAwsLambdaClient();

        if(getDescription() != null){
            request.withDescription(getDescription());
        }
        if(getRoutingConfig() != null){
            final RoutingConfig routingConfig = getRoutingConfig();

            final AliasRoutingConfiguration aliasRoutingConfiguration = routingConfig.getAliasRoutingConfiguration(lambda, functionName, functionVersion);

            request.withRoutingConfig(aliasRoutingConfiguration);
        }

        createAliasResult = lambda.createAlias(request);
    }

    private AWSLambda getAwsLambdaClient() {
        final AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
        return ext.getClient();
    }
}
