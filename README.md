Gradle AWS Plugin
=================

Gradle plugin to treat AWS resouces.

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
        classpath 'jp.classmethod.aws:gradle-aws-plugin:0.10'
      }
    }
    
    apply plugin: 'aws'
    
    aws {
      accessKeyId 'your-access-key'
      secretKey = 'your-secret-key'
      region = 'ap-northeast-1'
    }

These credentials are used to make API accesses by default.


### Implements tasks to start and stop bastion instance

    apply plugin: 'aws-ec2'
    
    // You can overwrite default credentials and region settings like this:
    // ec2 {
    //   region = 'us-east-1'
    // }
    
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


### Implements tasks to treat Elastic Beanstalk environemnt

    apply plugin: 'aws-beanstalk'
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
        envDesc = 'foobar demo application development environemnt'
        templateName = 'development'
        versionLabel = "foobar-${project.war.version}-${timestamp}"
      }
    }
    
    // task awsEbMigrateEnvironment, awsEbDeleteApplication and so on are declared


License
-------

Copyright (C) 2013-2014 [Classmethod, Inc.](http://classmethod.jp/)

Distributed under the Apache License v2.0.  See the file copyright/LICENSE.txt.
