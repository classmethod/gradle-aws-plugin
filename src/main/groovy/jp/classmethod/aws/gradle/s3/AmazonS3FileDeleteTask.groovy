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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*


class AmazonS3FileDeleteTask extends DefaultTask {
	
	{
		description = 'Delete file from the Amazon S3 bucket.'
		group = 'AWS'
	}

	String bucketName
	
	String key
	
	@TaskAction
	def delete() {
		if (! getBucketName()) throw new GradleException("bucketName is not specified")
		if (! getKey()) throw new GradleException("key is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		println "deleting... ${getBucketName()}/${getKey()}"
		s3.deleteObject(getBucketName(), getKey())
	}
}