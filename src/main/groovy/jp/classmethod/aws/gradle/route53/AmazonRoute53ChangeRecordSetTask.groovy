package jp.classmethod.aws.gradle.route53

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.amazonaws.*
import com.amazonaws.services.route53.*
import com.amazonaws.services.route53.model.*

class AmazonRoute53ChangeRecordSetTask extends DefaultTask {
	
	{
		description 'Create/Migrate Route53 Record.'
		group = 'AWS'
	}
	
	String hostedZoneId
	
	String rrsName
	
	String resourceRecord
	
	@TaskAction
	def changeResourceRecordSets() {
		AmazonRoute53PluginExtension ext = project.extensions.getByType(AmazonRoute53PluginExtension)
		AmazonRoute53 r53 = ext.r53
		
		r53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest()
			.withHostedZoneId(hostedZoneId)
			.withChangeBatch(new ChangeBatch()
				.withChanges(new Change(ChangeAction.CREATE, new ResourceRecordSet(rrsName, RRType.CNAME)
					.withResourceRecords(new ResourceRecord(resourceRecord))))))	
		println "change $hostedZoneId requested"
	}
}
