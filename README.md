Gradle AWS Plugin
=================

Current Features / Supported AWS Products
-----------------------------------------

* S3
  * Create or delete buckets
  * Upload or delete objects
  * File sync
* EC2
  * Start instance
  * Stop instance
  * Authorize security group permissions
  * Wait instance for specific status
* ELB
  * (TBD)
* Route53
  * Create or delete hosted zones
  * Change record sets
* CloudFormation
  * Migrate (create or update) stacks
  * Delete stacks
  * Wait stack for specific status
* Elastic Beanstalk
  * Create or delete applications
  * Create or terminate environments
  * Create or delete configuration templates
  * Create or delete application versions
  * Wait environment for specific status

How to use?
-----------

Add like this to your build.gradle :

    buildscript {
      repositories {
        mavenCentral()
        maven { url 'http://public-maven.classmethod.info/release' }
      }
      dependencies {
        classpath 'jp.classmethod.aws:gradle-aws-plugin:0.7'
      }
    }
    
    apply plugin: 'aws'
    
    aws {
      accessKeyId 'your-access-key'
      secretKey = 'your-secret-key'
      region = 'ap-northeast-1'
    }


### Implements tasks to start and stop bastion instance

    apply plugin: 'aws-ec2'
    
    task stopBastion(type: jp.classmethod.aws.gradle.ec2.AmazonEC2StopInstanceTask) {
      instanceIds += 'i-12345678'
    }

    task startBastion(type: jp.classmethod.aws.gradle.ec2.AmazonEC2StartInstanceTask) {
      instanceIds += 'i-12345678'
    }

### Implements sync S3 files task

    apply plugin: 'aws-s3'
    
    task syncObjects(type: jp.classmethod.aws.gradle.s3.SyncTask) {
      bucketName 'foobar.example.com'
      source file('path/to/objects')
    }


### Implements tasks to migrate and delete stack

    apply plugin: 'aws-cloudformation'
    
    cloudFormation {
      stackName 'foobar-stack'
      stackParams([
        Foo: 'bar',
        Baz: 'qux'
      ])
      capabilityIam true
      templateFile project.file("foobar.template")
      templateBucket 'example-bucket'
      templateKeyPrefix 'foobar/'
    }
    
    // awsCfnMigrateStack and awsCfnDeleteStack task (and so on) is declared.


### Implemets create / delete hosted zone task

    apply plugin: 'aws-route53'
    route53 {
      hostedZone 'foobar.example.com'
      callerReference '0BF44985-9D79-BF3B-A9B0-5AE24D6E86E1'
    }
    
    // awsR53CreateHostedZone task is declared


License
-------

Copyright (C) 2013-2014 [Classmethod, Inc.](http://classmethod.jp/)

Distributed under the Apache License v2.0.  See the file copyright/LICENSE.txt.


