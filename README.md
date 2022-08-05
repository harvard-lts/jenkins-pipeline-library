# Jenkins Pipeline Library
This is a shared library for resuable pipelines 

## How to use library in your project pipeline

### Jenkinsfile
``` 
#!groovy
@Library('lts-basic-pipeline') _

// projName: The directory name for the project on the servers for it's docker/config files
// intTestPort: port of integration test container
// intTestEndpoints: List of integration test endpoints i.e. ['healthcheck/', 'another/example/']
// default values: slackChannel = "lts-jenkins-notifications"

def endpoints = []
ltsBasicPipeline.call("<imageName>", "<stackName>", "<projName>", "<intTestPort>", endpoints, "<slackChannel>") 
```

NOTE: don't exclude the '_' at the end of the import line
