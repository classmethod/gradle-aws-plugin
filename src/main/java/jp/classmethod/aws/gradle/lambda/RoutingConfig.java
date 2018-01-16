package jp.classmethod.aws.gradle.lambda;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AliasRoutingConfiguration;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionRequest;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionResult;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by frankfarrell on 16/01/2018.
 *
 * This a bit different than the aws api.
 */
public class RoutingConfig {

    @Getter
    @Setter
    private Double additionalVersionWeight;

    @Getter
    @Setter
    private Boolean usePreviousVersion;

    @Getter
    @Setter
    private Boolean useNextVersion;

    @Getter
    @Setter
    private String additionalVersion;

    public RoutingConfig() {
    }

    public AliasRoutingConfiguration getAliasRoutingConfiguration(final AWSLambda lambda,
                                                                  final String functionName,
                                                                  final String functionVersion){
        if(getAdditionalVersionWeight() == null){
            throw new GradleException("Additional Version Weight for routing config is required");
        }
        if(getAdditionalVersion() == null ||
                getUsePreviousVersion() == null ||
                getUseNextVersion() ==null){
            throw new GradleException("Exactly one of AdditionalVersion, UsePreviousVersion, UseNextVersion for routing config is required");
        }

        final Double additionalVersionWeight = getAdditionalVersionWeight();

        final AliasRoutingConfiguration aliasRoutingConfiguration = new AliasRoutingConfiguration();

        if(getAdditionalVersion() != null){
            aliasRoutingConfiguration.withAdditionalVersionWeights(Collections.singletonMap(getAdditionalVersion(), additionalVersionWeight));
        }
        else if(getUsePreviousVersion() != null){
            final String prevVersion = getPreviousVersion(lambda, functionName, functionVersion);
            aliasRoutingConfiguration.withAdditionalVersionWeights(Collections.singletonMap(prevVersion, additionalVersionWeight));
        }
        else{
            final String nextVersion = getNextVersion(lambda, functionName, functionVersion);
            aliasRoutingConfiguration.withAdditionalVersionWeights(Collections.singletonMap(nextVersion, additionalVersionWeight));
        }
        return aliasRoutingConfiguration;
    }

    private String getNextVersion(final AWSLambda lambda,
                                  final String functionName,
                                  final String functionVersion) {
        final List<String> versions = getFunctionVersions(lambda, functionName);

        for(int i=1;i<versions.size();i++){
            if(versions.get(i).equals(functionVersion)){
                return versions.get(i-1);
            }
        }
        throw new GradleException("There is no newer version than " + functionName);
    }

    private String getPreviousVersion(final AWSLambda lambda,
                                      final String functionName,
                                      final String functionVersion) {

        final List<String> versions = getFunctionVersions(lambda, functionName);

        for(int i=0;i<versions.size()-1;i++){
            if(versions.get(i).equals(functionVersion)){
                return versions.get(i+1);
            }
        }
        throw new GradleException("There is no older version than " + functionName);
    }

    /*
    NB: Assumption here is that this returns an ordered list of versions from newest to oldest.
    It will only be a partial list since there could be pagination. We only return first page
     */
    private List<String> getFunctionVersions(final AWSLambda lambda,
                                             final String functionName) {
        final ListVersionsByFunctionRequest request = new ListVersionsByFunctionRequest().withFunctionName(functionName);
        final ListVersionsByFunctionResult result = lambda.listVersionsByFunction(request);
        return result.getVersions().stream().map(FunctionConfiguration::getVersion).collect(Collectors.toList());
    }
}
