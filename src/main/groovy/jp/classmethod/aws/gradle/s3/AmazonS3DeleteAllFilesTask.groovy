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


class AmazonS3DeleteAllFilesTask extends DefaultTask {
	
	{
		description = 'Delete all files on S3 bucket.'
		group = 'AWS'
	}

	String bucketName
	
	String prefix = ''
	
	@TaskAction
	def delete() {
		if (! getBucketName()) throw new GradleException("bucketName is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3

		String prefix = getPrefix()
		if (prefix.startsWith('/')) {
			prefix = prefix.substring(1)
		}
		
		println "deleting... ${getBucketName()}/${prefix}"
		
		List<S3ObjectSummary> objectSummaries
		while ((objectSummaries = s3.listObjects(getBucketName(), prefix).objectSummaries).isEmpty() == false) {
			objectSummaries.each { S3ObjectSummary os ->
				println "deleting... s3://${getBucketName()}/${os.key}"
				s3.deleteObject(getBucketName(), os.key)
			}
		}
	}
}