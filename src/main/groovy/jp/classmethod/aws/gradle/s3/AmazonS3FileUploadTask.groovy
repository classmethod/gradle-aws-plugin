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

import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.List;

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*

import com.google.common.hash.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction


class AmazonS3FileUploadTask extends AbstractAmazonS3FileUploadTask {
	{
		description = 'Upload file to the Amazon S3 bucket.'
		group = 'AWS'
	}


	@TaskAction
	def upload() {
		verifyParameters()

		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3Client s3 = ext.s3

		// metadata will be null iff the object does not exist
		def metadata = objectMetadata()

		if (overwrite || !metadata || (metadata.getETag() != md5())) {
			println "uploading... ${getBucketName()}/${getKey()}"
			resourceUrl = s3.getResourceUrl(getBucketName(), getKey())
			s3.putObject(getBucketName(), getKey(), getFile())
			println "upload completed: $resourceUrl"
		} else {
			println "${getBucketName()}/${getKey()} already exists with matching md5 sum -- skipped"
		}
	}

	def md5() {
		Hashing.md5().newHasher().putBytes(getFile().getBytes()).hash().toString()
	}

	def private verifyParameters() {
		if (!getBucketName()) throw new GradleException("bucketName is not specified")
		if (!getKey()) throw new GradleException("key is not specified")
		if (!getFile()) throw new GradleException("file is not specified")
	}
}