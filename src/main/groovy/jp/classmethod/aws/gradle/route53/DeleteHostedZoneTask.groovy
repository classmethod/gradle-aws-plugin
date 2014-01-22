package jp.classmethod.aws.gradle.route53

import com.amazonaws.services.route53.AmazonRoute53
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class DeleteHostedZoneTask extends DefaultTask {
	
	def String hostedZoneId
	
	@TaskAction
	def createHostedZone() {
		AmazonRoute53PluginExtension ext = project.extensions.getByType(AmazonRoute53PluginExtension)
		AmazonRoute53 r53 = ext.r53
		
		r53.deleteHostedZone(new DeleteHostedZoneRequest(hostedZoneId))
	}
}
