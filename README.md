Gradle AWS Plugin
=================

[![Join the chat at https://gitter.im/gradle-aws-plugin/Lobby](https://badges.gitter.im/gradle-aws-plugin/Lobby.svg)](https://gitter.im/gradle-aws-plugin/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Gradle plugin to manage AWS resources.

Current Features / Supported AWS Products
-----------------------------------------

* S3
  * Create bucket
  * Delete bucket
  * Upload object(s)
  * Delete object(s)
  * File sync
  * Set bucket policy
* EC2
  * Run instance
  * Start instance
  * Stop instance
  * Terminate instance
  * Import key
  * Create security group
  * Delete security group
  * Authorize security group ingress permissions
  * Authorize security group egress permissions
  * Revoke security group ingress permissions
  * Revoke security group egress permissions
  * Wait for specific status on instance
* RDS
  * Create DB instance
  * Delete DB instance
  * Modify DB instance
  * Migrate (create or modify) DB instance
  * Reboot DB instance
  * Wait for specific status on DB instance
* Route53
  * Create hosted zone
  * Delete hosted zone
  * Change record set
* Elastic Beanstalk
  * Create or delete applications
  * Create or terminate environments
  * Create or delete configuration templates
  * Create or delete application versions
  * Wait for specific status on environment
* CloudFormation
  * Migrate (create or update) stack
  * Delete stack
  * Wait for specific status on stack
* Lambda
  * Create function
  * Update function code
  * Update function configuration
  * Migrate (create or update) function
  * Invoke function
  * Delete function
  * Publish function version
  * Create alias
  * Update alias
* IAM
  * Create role
  * Attach role policy
* ELB
  * (TBD)
* SQS
  * Send messages
  * Delete messages
  * Read messages
* SNS
  * Publish message
* SSM
  * Put parameters

Requirements
------------

* Java 8+
* Gradle 4.6+

Usage
-----

Add like this to your `build.gradle`:

```groovy
import com.amazonaws.auth.profile.ProfileCredentialsProvider

buildscript {
  repositories {
    jcenter()
    mavenCentral()
    maven { url "https://plugins.gradle.org/m2/" }
  }
  dependencies {
    classpath "jp.classmethod.aws:gradle-aws-plugin:1.+"
  }
}

apply plugin: 'jp.classmethod.aws'

aws { // optional configurations
  clientBuilderConfig { 
    region = 'ap-northeast-1'
    credentials = new ProfileCredentialsProvider("default")
  }
}
```

You can configure AWS client builders in `clientBuilderConfig` block.

### S3 Create bucket

```groovy
apply plugin: 'jp.classmethod.aws.s3'

task createBucket(type: CreateBucketTask) {
	bucketName = myBucketName

	// one of http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region values, us-east-1 by default
	region = regionName
	// create bucket only if it does not exist, otherwise skip
	ifNotExists = true
}
```

Look at [S3 example 1](samples/01-s3-upload-simple) for more information.

### S3 files tasks

```groovy
apply plugin: 'jp.classmethod.aws.s3'

task syncObjects(type: jp.classmethod.aws.gradle.s3.SyncTask) {
  bucketName = 'foobar.example.com'
  source = file('path/to/objects')
}
```

Look at [S3 example 1](samples/01-s3-upload-simple) and [S3 example 2](samples/02-s3-sync-contents) for more information.


### EC2 instance tasks

```groovy
apply plugin: 'jp.classmethod.aws.ec2'

// You can overwrite common builder configuration settings like this:
// aws {
//   ec2 {
//     clientBuilderConfig { 
//       region = 'us-east-1'
//     }
//   }
// }

task stopBastion(type: jp.classmethod.aws.gradle.ec2.AmazonEC2StopInstanceTask) {
  instanceIds += 'i-12345678'
}

task startBastion(type: jp.classmethod.aws.gradle.ec2.AmazonEC2StartInstanceTask) {
  instanceIds += 'i-12345678'
}
```

Look at [EC2 example](samples/03-ec2) for more information.


### RDS DB instance tasks

```groovy
apply plugin: "jp.classmethod.aws.rds"

// You can overwrite common builder configuration settings like this:
// aws {
//   rds {
//     clientBuilderConfig { 
//       region = 'us-east-1'
//     }
//   }
// }

task migrateDBInstance(type: AmazonRDSMigrateDBInstanceTask) {
	dbInstanceIdentifier = "foobar"
	allocatedStorage = 5
	dbInstanceClass = "db.t2.micro"
	engine = "MySQL"
	masterUsername = "root"
	masterUserPassword = "passW0rd"
	vpcSecurityGroupIds = [ "sg-d3958fbf" ]
	dbSubnetGroupName = "default"
	multiAZ = false
	publiclyAccessible = true
}

task rebootDBInstance(type: AmazonRDSRebootDBInstanceTask) {
	dbInstanceIdentifier = "foobar"
}

task deleteDBInstance(type: AmazonRDSDeleteDBInstanceTask) {
	dbInstanceIdentifier = "foobar"
	skipFinalSnapshot = true
}
```

Look at [RDS example](samples/07-rds) for more information.


### Route 53 hosted zone tasks

```groovy
apply plugin: 'jp.classmethod.aws.route53'

task createHostedZone(type: jp.classmethod.aws.gradle.route53.CreateHostedZoneTask) {
	hostedZoneName "foobar.example.local"
	callerReference '0BF44985-9D79-BF3B-A9B0-5AE24D6E86E1'
}

task deleteHostedZone(type: jp.classmethod.aws.gradle.route53.DeleteHostedZoneTask) {
	hostedZoneId "XXXX"
}
```

Look at [Route 53 example](samples/04-route53) for more information.


### Elastic Beanstalk environment tasks

```groovy
apply plugin: 'jp.classmethod.aws.beanstalk'

aws {
  beanstalk {
    String extension = project.war.archiveName.tokenize('.').last()
    String timestamp = new Date().format("yyyyMMdd'_'HHmmss", TimeZone.default)

    appName 'foobar'
    appDesc 'foobar demo application'
  
    version {
      label = "foobar-${project.war.version}-${timestamp}"
      description = "${artifactId} v${version}"
      bucket = 'sample-bucket'
      key = "eb-apps/foobar-${project.war.version}-${timestamp}.${extension}"
    }
  
    configurationTemplates {
      production {
        optionSettings = file('src/main/config/production.json')
        solutionStackName = '64bit Amazon Linux 2013.09 running Tomcat 7 Java 7'
      }
      development {
        optionSettings = file('src/main/config/development.json')
        solutionStackName = '64bit Amazon Linux 2013.09 running Tomcat 7 Java 7'
      }
    }
  
    environment {
      envName = 'foobar'
      envDesc = 'foobar demo application development environment'
      templateName = 'development'
      versionLabel = "foobar-${project.war.version}-${timestamp}"
    }
  }
}

// task awsEbMigrateEnvironment, awsEbDeleteApplication and so on are declared
```

Look [Elastic Beanstalk example](samples/05-beanstalk) for more information.


### CloudFormation stack tasks

```groovy
apply plugin: 'jp.classmethod.aws.cloudformation'

aws {
  cloudFormation {
    stackName 'foobar-stack'
    stackParams([
      Foo: 'bar',
      Baz: 'qux'
    ])
    stackTags([
      Bar: 'foo',
      Baz: 'fox'
    })
    capabilityIam true
    templateFile project.file("foobar.template")
    templateBucket 'example-bucket'
    templateKeyPrefix 'foobar/'
  }
}

// awsCfnMigrateStack and awsCfnDeleteStack task (and so on) are declared.
```

Look at [CloudFormation example](samples/06-cloudformation) for more information.


### Lambda function tasks

```groovy
apply plugin: "base"
apply plugin: "jp.classmethod.aws.lambda"

aws {
  clientBuilderConfig { 
    region = 'ap-northeast-1' // ignored
    credentials = new ProfileCredentialsProvider("default")
  }
  lambda {
    region = 'us-east-1' // override
  }
}

task zip(type: Zip) {
  from "function/"
  destinationDir file("build")
}

task migrateFunction(type: AWSLambdaMigrateFunctionTask, dependsOn: zip) {
  functionName = "foobar"
  role = "arn:aws:iam::${aws.accountId}:role/lambda-poweruser"
  zipFile = zip.archivePath
  handler = "DecodeBase64.handler"
  alias = 'DEV'
  environment = [
    p1: "Value",
    p2: "Value2"
  ]
  tags = [
    p1: "Value",
    p2: "Value2"    
  ]
}

task invokeFunction(type: AWSLambdaInvokeTask) {
  functionName = "foobar"
  invocationType = InvocationType.RequestResponse
  payload = file("sample-input/input.txt")
  doLast {
    println "Lambda function result: " + new String(invokeResult.payload.array(), "UTF-8")
  }
}

task deleteFunction(type: AWSLambdaDeleteFunctionTask) {
  functionName = "foobar"
}

task publishVersionFunction(type: AWSLambdaPublishVersionTask, dependsOn: migrateFunction) {
	functionName = "foobar"
}

task createAlias(type: AWSLambdaCreateAliasTask, dependsOn: publishVersionFunction) {
	functionName = "foobar"
	aliasName = "alias"
	functionVersion = "1"
}

task updateAlias(type: AWSLambdaUpdateAliasTask, dependsOn: createAlias) {
	functionName = "foobar"
    aliasName = "alias"
	functionVersion = "1"
    routingConfig {
        additionalVersionWeight = 0.7
		useNextVersion = true
    }
}

task updateLambdaFunctionCode(type: AWSLambdaUpdateFunctionCodeTask) {
    functionName = "fooBar"
    zipFile = zip.archivePath
}
```

Look at [Lambda example](samples/08-lambda) for more information.

### SQS tasks

```groovy
apply plugin: "jp.classmethod.aws.sqs"

task sendMessages(type: AmazonSQSSendMessagesTask) {
  queueName 'gradle-aws-plugin-sample'
  messages Stream.of("Test 1", "Test 2")
}

task deleteMessages(type: AmazonSQSMessageConsumerTask) {
  queueName 'gradle-aws-plugin-sample'
  showMessages false
}

task viewMessages(type: AmazonSQSMessageConsumerTask) {
  queueName 'gradle-aws-plugin-sample'
  deleteMessages false
  maxNumberOfMessages 50
}
```

Look at [SQS example](samples/09-sqs) for more information.

### SNS tasks
```groovy
apply plugin: "jp.classmethod.aws.sns"

task publishMessage(type: AmazonSNSPublishMessageTask) {
  topicArn 'arn:aws:sns:us-east-1:000000000000:gradle-aws-plugin-sns-topic'
  message 'Test body'
  subject 'Optional test subject'
}

task publishJsonMessage(type: AmazonSNSPublishMessageTask) {
  topicArn 'arn:aws:sns:us-east-1:000000000000:gradle-aws-plugin-sns-topic'
  message JsonOutput.toJson(['default': 'Default message body.',
                             'email'  : 'Email message body.',
                             'sms': 'SMS message body.'])
  messageStructure 'json'
}
```
Look at [SNS example](samples/10-sns) for more information.

License
-------
Copyright (C) 2013-2018 [Classmethod, Inc.](http://classmethod.jp/)

Distributed under the Apache License v2.0.  See the file [copyright/LICENSE.txt](copyright/LICENSE.txt).

Development and Contribution
----------------------------
We are open to contributions.

To contribute to the plugin or make your own modifications, including the ability
to publish your build artifacts to your own Maven repository see: [development](docs/development.md).
