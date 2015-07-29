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
package jp.classmethod.aws.gradle.s3;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class SyncTask extends ConventionTask {
	
	private static String md5(File file) {
		try {
			return Files.hash(file, Hashing.md5()).toString();
		} catch (IOException e) {
			return "";
		}
	}

	@Getter @Setter
	private String bucketName;
	
	@Getter @Setter
	private String prefix = "";
	
	@Getter @Setter
	private File source;
	
	@Getter @Setter
	private boolean delete;
	
	@Getter @Setter
	private Closure<ObjectMetadata> metadataProvider;
	
	@TaskAction
	public void uploadAction() {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String prefix = getPrefix();
		File source = getSource();

		if (bucketName == null) throw new GradleException("bucketName is not specified");
		if (source == null) throw new GradleException("source is not specified");
		if (source.isDirectory() == false) throw new GradleException("source must be directory");
		
		prefix = prefix.startsWith("/") ? prefix.substring(1) : prefix;

		AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
		AmazonS3 s3 = ext.getClient();

		upload(s3, prefix);
		if (isDelete()) {
			deleteAbsent(s3, prefix);
		}
	}
	
	private void upload(AmazonS3 s3, String prefix) {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		File source = getSource();

		getLogger().info("uploading... {} to s3://{}/{}", bucketName, bucketName, prefix);
		getProject().fileTree(source).visit(new EmptyFileVisitor() {
			public void visitFile(FileVisitDetails element) {
				String relativePath = prefix + element.getRelativePath().toString();
				String key = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
				
				boolean doUpload = false;
				try {
					ObjectMetadata metadata = s3.getObjectMetadata(bucketName, key);
					if (metadata.getETag().equalsIgnoreCase(md5(element.getFile())) == false) {
						doUpload = true;
					}
				} catch (AmazonS3Exception e) {
					doUpload = true;
				}
				
				if (doUpload) {
					getLogger().info(" => s3://"+bucketName+"/"+key);
					Closure<ObjectMetadata> metadataProvider = getMetadataProvider();
					s3.putObject(new PutObjectRequest(getBucketName(), key, element.getFile())
						.withMetadata(metadataProvider == null ? null : metadataProvider.call(getBucketName(), key, element.getFile())));
				} else {
					getLogger().info(" => s3://{}/{} (SKIP)", bucketName, key);
				}
			}
		});
	}
	
	private void deleteAbsent(AmazonS3 s3, String prefix) {
		// to enable conventionMappings feature
		String bucketName = getBucketName();
		String pathPrefix = getNormalizedPathPrefix();
		
		s3.listObjects(bucketName, prefix).getObjectSummaries().forEach(os -> {
			File f = getProject().file(pathPrefix + os.getKey().substring(prefix.length()));
			if (f.exists() == false) {
				getLogger().info("deleting... s3://{}/{}", bucketName, os.getKey());
				s3.deleteObject(bucketName, os.getKey());
			}
		});
	}

	private String getNormalizedPathPrefix() {
		String pathPrefix = getSource().toString();
		pathPrefix += pathPrefix.endsWith("/") ? "" : "/";
		return pathPrefix;
	}
}
