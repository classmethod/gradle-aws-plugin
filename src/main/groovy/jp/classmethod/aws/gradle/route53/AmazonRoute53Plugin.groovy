package jp.classmethod.aws.gradle.route53

import groovy.lang.Lazy;
import jp.classmethod.aws.gradle.AwsPluginExtension

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.amazonaws.*
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
	
	AmazonRoute53PluginExtension getExtension(Project project) {
		project[AmazonRoute53PluginExtension.NAME]
	}
	
	void applyTasks(final Project project) {
		def awsCfnUploadTemplate = project.task('awsR53CreateHostedZone', type: CreateHostedZoneTask) { CreateHostedZoneTask t ->
			t.description = 'Create hostedZone.'
			t.doFirst {
				t.hostedZoneName = getExtension(project).hostedZone
				t.callerReference = getExtension(project).callerReference
			}
		}
	}
}

class AmazonRoute53PluginExtension {
	
	public static final NAME = 'route53'
	
	Project project;
		
	@Lazy
	def AmazonRoute53 r53 = {
		AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
		aws.configureRegion(new AmazonRoute53Client(aws.credentialsProvider))
	}()
	
	def String hostedZone
	def String callerReference
	
	AmazonRoute53PluginExtension(Project project) {
		this.project = project;
	}
	
	def String getHostedZoneId() {
		def ListHostedZonesResult lhzr = r53.listHostedZones()
		def HostedZone zone = lhzr.hostedZones.find { it.name == (hostedZone + ".") }
		if (zone == null) {
			throw new GradleException("Hosted zone ${hostedZone} not found.")
		}
		zone.id
	}
	
	def ResourceRecordSet getAssociatedResourceRecordSet(String hostname) {
		def String resourceRecordName = hostname - hostedZone
		def ListResourceRecordSetsResult lrrsr = r53.listResourceRecordSets(new ListResourceRecordSetsRequest(hostedZoneId)
			.withStartRecordName(resourceRecordName))
		lrrsr.resourceRecordSets.find { it.type == 'CNAME' || it.aliasTarget != null }
	}
	
	def void associateAsAlias(String hostname, LoadBalancerDescription ldb, ResourceRecordSet oldResourceRecordSet = null) {
		def String resourceRecordName = hostname - hostedZone
		
		def List<Change> changes = []
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
	
	def void swapAlias(String hostname, List<LoadBalancerDescription> ldbs, ResourceRecordSet oldResourceRecordSet = null) {
		def String oldDNSName = oldResourceRecordSet?.aliasTarget.dNSName.toLowerCase()
		println "oldDNSName = $oldDNSName"
		LoadBalancerDescription ldb = ldbs.find { (it.dNSName.toLowerCase() + '.') != oldDNSName }
		println "newDNSName = ${ldb.dNSName.toLowerCase()}."
		associateAsAlias(hostname, ldb, oldResourceRecordSet)
	}
}
