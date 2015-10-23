/*
 * Copyright 2013-2015 Classmethod, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.lambda;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;

public class S3File {
    @Getter @Setter
    private String bucketName;

    @Getter @Setter
    private String key;

    @Getter @Setter
    private String objectVersion;

    /**
     * Validates that both bucketName and key are provided.
     */
    public void validate() {
        boolean missingBucketName = bucketName == null || bucketName.trim().isEmpty();
        boolean missingKey = key == null || key.trim().isEmpty();
        if (missingBucketName || missingKey) {
            throw new GradleException("bucketName and key are required for an S3File");
        }
    }
}
