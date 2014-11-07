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
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*


class BulkUploadTask extends DefaultTask {

	String bucketName
	
	String prefix
	
	FileTree source
	
	@TaskAction
	def upload() {
		// to enable conventionMappings feature
		String bucketName = getBucketName()
		String prefix = getPrefix()
		FileTree source = getSource()

		if (prefix.startsWith('/')) {
			prefix = prefix.substring(1)
		}
		if (prefix.endsWith('/') == false) {
			prefix += '/'
		}

		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3
		
		logger.info "uploading... $source to s3://$bucketName/$prefix"
		getSource().visit { FileTreeElement element ->
			if (element.isDirectory() == false) {
				String key = prefix + element.relativePath
				logger.info " => s3://$bucketName/$key"
				s3.putObject(bucketName, key, element.file)
			}
		}
	}
}
