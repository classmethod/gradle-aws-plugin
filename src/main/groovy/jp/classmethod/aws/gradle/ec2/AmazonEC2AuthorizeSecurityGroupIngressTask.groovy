package jp.classmethod.aws.gradle.ec2

import java.util.List

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.amazonaws.*
import com.amazonaws.services.ec2.*
import com.amazonaws.services.ec2.model.*


class AmazonEC2AuthorizeSecurityGroupIngressTask extends AbstractAmazonEC2SecurityGroupPermissionTask {
	
	{
		description 'Authorize security group ingress.'
		group = 'AWS'
	}
	
	String groupId
	
	def ipPermissions
	
	@TaskAction
	def createApplication() {
		if (! groupId) throw new GradleException("groupId is not specified")
		if (! ipPermissions) throw new GradleException("ipPermissions is not specified")
		
		AmazonEC2PluginExtension ext = project.extensions.getByType(AmazonEC2PluginExtension)
		AmazonEC2 ec2 = ext.ec2
		
		try {
			ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest()
				.withGroupId(groupId)
				.withIpPermissions(parse(toCollection(ipPermissions))))
		} catch (AmazonServiceException e) {
			if (e.getErrorCode() == 'InvalidPermission.Duplicate') {
				logger.warn(e.getMessage())
			} else {
				throw e
			}
		}
	}
}
