package jp.classmethod.aws.gradle.elasticbeanstalk;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier;


public enum Tier {
	
	WebServer(new EnvironmentTier()
		.withType("Standard")
		.withName("WebServer")
		.withVersion("1.0")),
	
	Worker(new EnvironmentTier()
		.withType("SQS/HTTP")
		.withName("Worker")
		.withVersion("1.0"));
	
	final EnvironmentTier environmentTier;
	
	Tier(EnvironmentTier environmentTier) {
		this.environmentTier = environmentTier;
	}
	
	public EnvironmentTier toEnvironmentTier() {
		return environmentTier;
	}
}
