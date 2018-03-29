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
package jp.classmethod.aws.gradle.ec2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;

import com.amazonaws.services.ec2.model.IpPermission;

import jp.classmethod.aws.gradle.common.BaseAwsTask;

import groovy.lang.GString;

abstract class AbstractAmazonEC2SecurityGroupPermissionTask extends BaseAwsTask { // NOPMD
	
	public AbstractAmazonEC2SecurityGroupPermissionTask(String group, String description) {
		super(group, description);
	}
	
	Collection<IpPermission> parse(Object e) { // NOPMD
		if (e instanceof IpPermission) {
			return Collections.singleton((IpPermission) e);
		}
		Collection<?> elements;
		if (e instanceof Collection) {
			elements = (Collection<?>) e;
		} else {
			elements = Collections.singleton(e);
		}
		
		return elements.stream().map(it -> {
			if (it instanceof IpPermission) {
				return (IpPermission) it;
			} else if (it instanceof String || it instanceof GString) {
				// "tcp/22:10.0.0.2/32"
				// "tcp/1-65535:10.0.0.2/32"
				// "tcp/22:10.0.0.2/32,10.0.0.5/32"
				
				String expression = it.toString();
				if (expression.contains(":") == false) {
					throw new ParseException(expression);
				}
				
				String protocol;
				int fromPort;
				int toPort;
				
				String[] expressions = expression.split(":", 2);
				String protocolAndPortExpression = expressions[0];
				String rangeExpression = expressions[1];
				
				if ("icmp".equalsIgnoreCase(protocolAndPortExpression)) {
					protocol = protocolAndPortExpression;
					fromPort = toPort = -1;
				} else if (protocolAndPortExpression.contains("/") == false) {
					protocol = protocolAndPortExpression;
					fromPort = 0;
					toPort = 65535;
				} else {
					String[] protocolAndPortExpressions = protocolAndPortExpression.split("/", 2);
					protocol = protocolAndPortExpressions[0];
					String portExpression = protocolAndPortExpressions[1];
					if (portExpression.contains("-")) {
						String[] ports = portExpression.split("-", 2);
						fromPort = Integer.parseInt(ports[0]);
						toPort = Integer.parseInt(ports[1]);
					} else {
						fromPort = toPort = Integer.parseInt(portExpression);
					}
				}
				
				List<String> ranges = Arrays.asList(rangeExpression.split(","));
				
				return new IpPermission()
					.withIpProtocol(protocol)
					.withFromPort(fromPort)
					.withToPort(toPort)
					.withIpRanges(ranges);
			} else {
				throw new GradleException("ipPermission type only supports IpPermission or String: " + it.getClass());
			}
		}).collect(Collectors.toList());
	}
}
