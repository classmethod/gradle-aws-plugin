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
package jp.classmethod.aws.gradle.route53

import groovy.lang.Lazy;
import jp.classmethod.aws.gradle.AwsPluginExtension

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
import com.amazonaws.regions.*
import com.amazonaws.services.route53.*
import com.amazonaws.services.route53.model.*
import com.amazonaws.services.elasticloadbalancing.*
import com.amazonaws.services.elasticloadbalancing.model.*


class AmazonRoute53Plugin implements Plugin<Project> {
	
	void apply(Project project) {
		project.configure(project) {
			apply plugin: 'aws'
			project.extensions.create(AmazonRoute53PluginExtension.NAME, AmazonRoute53PluginExtension, project)
			applyTasks(project)
		}
	}
	
	void applyTasks(final Project project) {
		AmazonRoute53PluginExtension r53Ext = project.extensions.getByType(AmazonRoute53PluginExtension)
		
		def awsCfnUploadTemplate = project.task('awsR53CreateHostedZone', type: CreateHostedZoneTask) { CreateHostedZoneTask t ->
			t.description = 'Create hostedZone.'
			t.doFirst {
				t.hostedZoneName = r53Ext.hostedZone
				t.callerReference = r53Ext.callerReference
			}
		}
	}
}

class AmazonRoute53PluginExtension {
	
	public static final NAME = 'route53'
	
	Project project
	String accessKeyId
	String secretKey
	Region region
		
	@Lazy
	AmazonRoute53 r53 = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		return aws.createClient(AmazonRoute53Client, region, accessKeyId, secretKey)
	}()
	
	String hostedZone
	String callerReference
	
	AmazonRoute53PluginExtension(Project project) {
		this.project = project;
	}
	
	String getHostedZoneId() {
		ListHostedZonesResult lhzr = r53.listHostedZones()
		HostedZone zone = lhzr.hostedZones.find { it.name == (hostedZone + ".") }
		if (zone == null) {
			throw new GradleException("Hosted zone ${hostedZone} not found.")
		}
		return zone.id
	}
	
	ResourceRecordSet getAssociatedResourceRecordSet(String hostname) {
		String resourceRecordName = hostname - hostedZone
		ListResourceRecordSetsResult lrrsr = r53.listResourceRecordSets(new ListResourceRecordSetsRequest(hostedZoneId)
			.withStartRecordName(resourceRecordName))
		return lrrsr.resourceRecordSets.find { it.type == 'CNAME' || it.aliasTarget != null }
	}
	
	void associateAsAlias(String hostname, LoadBalancerDescription ldb, ResourceRecordSet oldResourceRecordSet = null) {
		String resourceRecordName = hostname - hostedZone
		
		List<Change> changes = []
		if (oldResourceRecordSet != null) {
			changes += new Change(ChangeAction.DELETE, oldResourceRecordSet)
		}
		changes += new Change(ChangeAction.CREATE, new ResourceRecordSet(hostname, RRType.A)
			.withAliasTarget(new AliasTarget(ldb.canonicalHostedZoneNameID, ldb.canonicalHostedZoneName)
				.withEvaluateTargetHealth(false)))
		
		r53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest()
			.withHostedZoneId(getHostedZoneId())
			.withChangeBatch(new ChangeBatch().withChanges(changes)))
	}
	
	void swapAlias(String hostname, List<LoadBalancerDescription> ldbs, ResourceRecordSet oldResourceRecordSet = null) {
		String oldDNSName = oldResourceRecordSet?.aliasTarget.dNSName.toLowerCase()
		println "oldDNSName = $oldDNSName"
		LoadBalancerDescription ldb = ldbs.find { (it.dNSName.toLowerCase() + '.') != oldDNSName }
		println "newDNSName = ${ldb.dNSName.toLowerCase()}."
		associateAsAlias(hostname, ldb, oldResourceRecordSet)
	}
}
