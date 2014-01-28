package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class AWSElasticBeanstalkCleanupApplicationVersionTask extends DefaultTask {
	
	{
		description 'Cleanup unused SNAPSHOT ElasticBeanstalk Application Version.'
		group = 'AWS'
	}
	
	String applicationName
	
	boolean deleteSourceBundle = true

	
	@TaskAction
	def deleteVersion() {
		AwsBeanstalkPluginExtension ext = project.extensions.getByType(AwsBeanstalkPluginExtension)
		AWSElasticBeanstalk eb = ext.eb
		
		Set<String> usingVersions = []
		DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
			.withApplicationName(applicationName))
		for (EnvironmentDescription ed in der.environments) {
			usingVersions += ed.versionLabel
//			println "version ${ed.versionLabel} is using"
		}
		
		List<String> versionLabelsToDelete = []
		DescribeApplicationVersionsResult davr = eb.describeApplicationVersions(new DescribeApplicationVersionsRequest()
			.withApplicationName(applicationName))
		for (ApplicationVersionDescription avd in davr.applicationVersions) {
			if (usingVersions.contains(avd.versionLabel) == false
					&& avd.versionLabel.contains('-SNAPSHOT-')) {
				versionLabelsToDelete += avd.versionLabel
			}
		}
		
		for (String versionLabel in versionLabelsToDelete) {
			println "version ${versionLabel} deleted"
			eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
				.withApplicationName(applicationName)
				.withVersionLabel(versionLabel)
				.withDeleteSourceBundle(deleteSourceBundle))
		}
	}
}
