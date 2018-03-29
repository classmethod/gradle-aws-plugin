/*
 * Copyright 2015-2016 the original author or authors.
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
package jp.classmethod.aws.gradle.s3;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

import groovy.lang.Closure;

public class BulkUploadTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String bucketName;
	
	@Getter
	@Setter
	private String prefix;
	
	@Getter
	@Setter
	private FileTree source;
	
	@Getter
	@Setter
	private Closure<ObjectMetadata> metadataProvider;
	
	
	public BulkUploadTask() {
		super("AWS", "Upload bulk");
	}
	
	@TaskAction
	public void upload() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String prefix = getNormalizedPrefix();
		FileTree source = getSource();
		
		AmazonS3PluginExtension ext = getPluginExtension(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();
		
		getLogger().info("uploading... {} to s3://{}/{}", source, bucketName, prefix);
		source.visit(new EmptyFileVisitor() {
			
			public void visitFile(FileVisitDetails element) {
				String key = prefix + element.getRelativePath();
				getLogger().info(" => s3://{}/{}", bucketName, key);
				Closure<ObjectMetadata> metadataProvider = getMetadataProvider();
				s3.putObject(new PutObjectRequest(bucketName, key, element.getFile())
					.withMetadata(metadataProvider == null ? null
							: metadataProvider.call(getBucketName(), key, element.getFile())));
			}
		});
	}
	
	private String getNormalizedPrefix() {
		String prefix = getPrefix();
		if (prefix.startsWith("/")) {
			prefix = prefix.substring(1);
		}
		if (prefix.endsWith("/") == false) {
			prefix += "/";
		}
		return prefix;
	}
}
