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
package jp.classmethod.aws.gradle.cloudformation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.ChangeSetSummary;
import com.amazonaws.services.cloudformation.model.ListChangeSetsRequest;
import com.amazonaws.services.cloudformation.model.ListChangeSetsResult;

public class ChangeSetFetcher {
	
	private final AmazonCloudFormation cfn;
	
	
	public ChangeSetFetcher(AmazonCloudFormation cfn) {
		this.cfn = cfn;
	}
	
	/**
	 *
	 * Return the latest ChangeSet Summary for the specified CloudFormation stack.
	 * @param stackName name of the CloudFormation stack
	 * @return Optional
	 */
	public Optional<ChangeSetSummary> getLatestChangeSetSummary(String stackName) {
		
		ListChangeSetsResult changeSetsResult =
				cfn.listChangeSets(new ListChangeSetsRequest().withStackName(stackName));
		List<ChangeSetSummary> changeSetSummaries = changeSetsResult.getSummaries();
		if (changeSetSummaries.isEmpty()) {
			return Optional.empty();
		}
		
		changeSetSummaries.sort(Comparator.comparing(ChangeSetSummary::getCreationTime).reversed());
		return Optional.of(changeSetSummaries.get(0));
	}
}
