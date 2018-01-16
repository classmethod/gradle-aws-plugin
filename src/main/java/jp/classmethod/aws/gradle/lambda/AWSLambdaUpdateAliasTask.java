package jp.classmethod.aws.gradle.lambda;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.*;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by frankfarrell on 16/01/2018.
 *
 * https://docs.aws.amazon.com/cli/latest/reference/lambda/update-alias.html
 */
public class AWSLambdaUpdateAliasTask extends ConventionTask {

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
    private UpdateAliasResult updateAliasResult;


    public AWSLambdaUpdateAliasTask() {
        setDescription("Update Lambda Alias");
        setGroup("AWS");
    }

    @TaskAction
    public void aupdateFunctionAlias(){
        // to enable conventionMappings feature
        final String functionName = getFunctionName();
        final String aliasName = getName();

        if (functionName == null) {
            throw new GradleException("functionName is required");
        }
        if (aliasName == null) {
            throw new GradleException("name for alias is required");
        }

        final AWSLambda lambda = getAwsLambdaClient();

        final UpdateAliasRequest updateAliasRequest = new UpdateAliasRequest()
                .withFunctionName(functionName)
                .withName(aliasName);

        if(getFunctionVersion() != null){
            updateAliasRequest.withFunctionVersion(getFunctionVersion());
        }
        if(getDescription() != null){
            updateAliasRequest.withDescription(getDescription());
        }
        if(getRoutingConfig() != null){
            final RoutingConfig routingConfig = getRoutingConfig();

            if(routingConfig.getAdditionalVersionWeight() == null){
                throw new GradleException("Additional Version Weight for routing config is required");
            }
            if(routingConfig.getAdditionalVersion() == null ||
                    routingConfig.getUsePreviousVersion() == null ||
                    routingConfig.getUseNextVersion() ==null){
                throw new GradleException("Exactly one of AdditionalVersion, UsePreviousVersion, UseNextVersion for routing config is required");
            }

            final Double additionalVersionWeight = routingConfig.getAdditionalVersionWeight();


            final AliasRoutingConfiguration aliasRoutingConfiguration = new AliasRoutingConfiguration();

            if(routingConfig.getAdditionalVersion() != null){
                aliasRoutingConfiguration.withAdditionalVersionWeights(Collections.singletonMap(routingConfig.getAdditionalVersion(), additionalVersionWeight));
            }
            else if(routingConfig.getUsePreviousVersion() != null){
                final String prevVersion = getPreviousVersion(functionName, getFunctionVersion());
                aliasRoutingConfiguration.withAdditionalVersionWeights(Collections.singletonMap(prevVersion, additionalVersionWeight));
            }
            else{
                final String nextVersion = getNextVersion(functionName, getFunctionVersion());
                aliasRoutingConfiguration.withAdditionalVersionWeights(Collections.singletonMap(nextVersion, additionalVersionWeight));
            }
            updateAliasRequest.withRoutingConfig(aliasRoutingConfiguration);
        }

        updateAliasResult = lambda.updateAlias(updateAliasRequest);
        getLogger().info("Update Lambda alias requested: {}, name: {}", functionName, aliasName);
    }

    private String getNextVersion(final String functionName, final String functionVersion) {
        final List<String> versions = getFunctionVersions(functionName);

        for(int i=1;i<versions.size();i++){
            if(versions.get(i).equals(functionVersion)){
                getLogger().info("Next version after {} is {}", functionVersion, versions.get(i-1));
                return versions.get(i-1);
            }
        }
        throw new GradleException("There is no newer version than " + functionName);
    }

    private String getPreviousVersion(final String functionName, final String functionVersion) {

        final List<String> versions = getFunctionVersions(functionName);

        for(int i=0;i<versions.size()-1;i++){
            if(versions.get(i).equals(functionVersion)){
                getLogger().info("Version before {} is {}", functionVersion, versions.get(i+1));
                return versions.get(i+1);
            }
        }
        throw new GradleException("There is no older version than " + functionName);
    }

    /*
    NB: Assumption here is that this returns an ordered list of versions from newest to oldest.
    It will only be a partial list since there could be pagination. We only return first page
     */
    private List<String> getFunctionVersions(String functionName) {
        final ListVersionsByFunctionRequest request = new ListVersionsByFunctionRequest().withFunctionName(functionName);
        final AWSLambda lambda = getAwsLambdaClient();
        final ListVersionsByFunctionResult result = lambda.listVersionsByFunction(request);
        return result.getVersions().stream().map(FunctionConfiguration::getVersion).collect(Collectors.toList());
    }

    private AWSLambda getAwsLambdaClient() {
        final AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
        return ext.getClient();
    }
}
