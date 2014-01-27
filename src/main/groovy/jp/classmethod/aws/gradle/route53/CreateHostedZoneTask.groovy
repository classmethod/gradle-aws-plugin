package jp.classmethod.aws.gradle.route53

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.amazonaws.*
import com.amazonaws.services.route53.*
import com.amazonaws.services.route53.model.*

class CreateHostedZoneTask extends DefaultTask {
	
	String hostedZoneName
	
	String callerReference
	
	String comment
	
	@TaskAction
	def createHostedZone() {
		AmazonRoute53PluginExtension ext = project.extensions.getByType(AmazonRoute53PluginExtension)
		AmazonRoute53 r53 = ext.r53
		
		String callerRef = callerReference == null ? InetAddress.getLocalHost().getHostName() : callerReference
		println "callerRef = ${callerRef}"
		
		CreateHostedZoneRequest req = new CreateHostedZoneRequest()
			.withName(hostedZoneName)
			.withCallerReference(callerRef)
		if (comment != null) {
			req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment))
		}
		
		try {
			CreateHostedZoneResult chzr = r53.createHostedZone(req)
			println "HostedZone ${hostedZoneName} - ${callerRef} is created."
			for (String nameServer in chzr.delegationSet.nameServers) {
				println "NS ${nameServer}"
			}
		} catch (HostedZoneAlreadyExistsException e) {
			println "HostedZone ${hostedZoneName} - ${callerRef} is already created."
		}
	}
}
