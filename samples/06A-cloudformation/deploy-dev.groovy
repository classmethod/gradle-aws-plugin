aws {
  clientBuilderConfig {
    region = "ap-northeast-1"
  }
  cloudFormation {
    stackName = "HelloWorld-stack"
    capabilityIam = true
    templateFile = project.file('cloudformation/HelloWorld.yml')
    stackPolicyFile = project.file('cloudformation/stackpolicy.json')
    onFailure = 'DO_NOTHING'

    stackParams([
            'pArtifactBucket'   : 'my-deploy-dev',
            'pArtifactPrefix'   : 'helloworld-example',

            'pHelloWorldZipFile': "06A-cloudformation-${-> version}.zip"
    ])
  }
}

