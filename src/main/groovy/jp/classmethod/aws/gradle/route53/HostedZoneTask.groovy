package jp.classmethod.aws.gradle.route53

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.amazonaws.*
import com.amazonaws.services.route53.*
import com.amazonaws.services.route53.model.*

class CreateHostedZoneTask extends DefaultTask {
	
	def String hostedZoneName
	
	def String callerReference
	
	def String comment
	
	@TaskAction
	def createHostedZone() {
		def AmazonRoute53 r53 = project.aws.r53
		
		def String callerRef = callerReference == null ? InetAddress.getLocalHost().getHostName() : callerReference
		println "callerRef = ${callerRef}"
		def CreateHostedZoneRequest req = new CreateHostedZoneRequest()
			.withName(hostedZoneName)
			.withCallerReference(callerRef)
		if (comment != null) {
			req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment))
		}
		try {
			def CreateHostedZoneResult chzr = r53.createHostedZone(req)
			println "HostedZone ${hostedZoneName} - ${callerRef} is created."
			for (String nameServer in chzr.delegationSet.nameServers) {
				println "NS ${nameServer}"
			}
		} catch (HostedZoneAlreadyExistsException e) {
			println "HostedZone ${hostedZoneName} - ${callerRef} is already created."
		}
	}
}

class DeleteHostedZoneTask extends DefaultTask {
	
	def String hostedZoneId
	
	def String comment
	
	@TaskAction
	def createHostedZone() {
		def AmazonRoute53 r53 = project.aws.r53
		
		r53.deleteHostedZone(new DeleteHostedZoneRequest(hostedZoneId))
	}
}
