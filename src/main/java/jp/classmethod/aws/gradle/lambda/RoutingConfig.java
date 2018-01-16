package jp.classmethod.aws.gradle.lambda;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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
}
