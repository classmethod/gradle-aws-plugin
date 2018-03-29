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
package jp.classmethod.aws.gradle.route53;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

public class AmazonRoute53ChangeRecordSetTask extends BaseAwsTask {
	
	@Getter
	@Setter
	private String hostedZoneId;
	
	@Getter
	@Setter
	private String rrsName;
	
	@Getter
	@Setter
	private String resourceRecord;
	
	
	public AmazonRoute53ChangeRecordSetTask() {
		super("AWS", "Create/Migrate Route53 Record.");
	}
	
	@TaskAction
	public void changeResourceRecordSets() {
		// to enable conventionMappings feature
		String hostedZoneId = getHostedZoneId();
		String rrsName = getRrsName();
		String resourceRecord = getResourceRecord();
		
		AmazonRoute53PluginExtension ext = getPluginExtension(AmazonRoute53PluginExtension.class);
		AmazonRoute53 route53 = ext.getClient();
		
		route53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest()
			.withHostedZoneId(hostedZoneId)
			.withChangeBatch(new ChangeBatch()
				.withChanges(new Change(ChangeAction.CREATE, new ResourceRecordSet(rrsName, RRType.CNAME)
					.withResourceRecords(new ResourceRecord(resourceRecord))))));
		getLogger().info("change {} requested", hostedZoneId);
	}
}
