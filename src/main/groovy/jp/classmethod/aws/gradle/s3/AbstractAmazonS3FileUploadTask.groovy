/*
 * Copyright 2013-2014 Classmethod, Inc.
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
package jp.classmethod.aws.gradle.s3

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.internal.ConventionTask

abstract class AbstractAmazonS3FileUploadTask extends ConventionTask {

	String bucketName

	String key

	File file
	
	ObjectMetadata objectMetadata

	String resourceUrl

	boolean overwrite = false

	ObjectMetadata existingObjectMetadata() {
		// to enable conventionMappings feature
		String bucketName = getBucketName()
		String key = getKey()
		File file = getFile()
		boolean overwrite = isOverwrite()

		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		try {
			// to enable conventionMapping, you must reference field via getters
			return s3.getObjectMetadata(bucketName, key)
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() != 404) {
				throw e
			}
		}
		return null
	}

	boolean exists() {
		existingObjectMetadata() != null
	}
}
