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

import com.amazonaws.*;
import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction


public class CreateBucketTask extends DefaultTask {
	
	{
		description 'Create the Amazon S3 bucket.'
		group = 'AWS'
	}
	
	String bucketName
	
	boolean ifNotExists
	
	@TaskAction
	def createBucket() {
		if (! getBucketName()) throw new GradleException("bucketName is not specified")
		
		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		if (isIfNotExists() == false || exists(s3) == false) {
			s3.createBucket(getBucketName())
			println "bucket ${getBucketName()} created"
		}
	}
	
	boolean exists(AmazonS3 s3) {
		try {
			s3.getBucketLocation(getBucketName())
			return true
		} catch (AmazonClientException e) {
			return false
		}
	}
}
