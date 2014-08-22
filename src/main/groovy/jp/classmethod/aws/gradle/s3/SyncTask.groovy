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
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.*
import com.amazonaws.services.s3.model.*

class SyncTask extends DefaultTask {
	
	String bucketName
	
	String prefix = ''
	
	File source
	
	@TaskAction
	def uploadAction() {
		if (! getBucketName()) throw new GradleException("bucketName is not specified")
		if (! getSource()) throw new GradleException("source is not specified")
		
		String prefix = getPrefix()
		prefix = prefix.startsWith('/') ? prefix.substring(1) : prefix

		AmazonS3PluginExtension ext = project.extensions.getByType(AmazonS3PluginExtension)
		AmazonS3 s3 = ext.s3

		upload(s3, prefix)
		delete(s3, prefix)
	}
	
	private String upload(AmazonS3 s3, String prefix) {
		println "uploading... ${getSource()} to s3://${getBucketName()}/${prefix}"
		project.fileTree(getSource()).visit { FileTreeElement element ->
			if (element.isDirectory() == false) {
				String relativePath = prefix + element.relativePath.toString()
				String key = relativePath.startsWith('/') ? relativePath.substring(1) : relativePath
				
				String md5
				FileInputStream fis = null
				try {
					fis = new FileInputStream(element.file)
					md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
				} finally {
					if (fis != null) {
						fis.close()
					}
				}
				
				boolean doUpload = false
				try {
					def metadata = s3.getObjectMetadata(getBucketName(), key)
					if (metadata.ETag.equalsIgnoreCase(md5) == false) {
						doUpload = true
					}
				} catch (AmazonS3Exception e) {
					doUpload = true
				}
				
				if (doUpload) {
					println " => s3://${getBucketName()}/${key}"
					s3.putObject(getBucketName(), key, element.file)
				} else {
					println " => s3://${getBucketName()}/${key} (SKIP)"
				}
			}
		}
	}
	
	private delete(AmazonS3 s3, String prefix) {
		String pathPrefix = getSource().toString()
		pathPrefix += pathPrefix.endsWith('/') ? '' : '/'
		s3.listObjects(getBucketName(), prefix).objectSummaries.each { S3ObjectSummary os ->
			def File f = project.file(pathPrefix + os.key.substring(prefix.length()))
			if (f.exists() == false) {
				println "deleting... s3://${getBucketName()}/${os.key}"
				s3.deleteObject(getBucketName(), os.key)
			}
		}
	}
}
