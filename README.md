# Jenkins Pipeline Library
This is a shared library for resuable pipelines 

## How to use library in your project pipeline

### Jenkinsfile

#### Basic Pipeline
``` 
#!groovy
@Library('lts-basic-pipeline') _

// projName: The directory name for the project on the servers for it's docker/config files
// intTestPort: Port of integration test container
// intTestEndpoints: List of integration test endpoints i.e. ['healthcheck/', 'another/example/']
// default values: slackChannel = "lts-jenkins-notifications"

def endpoints = []
ltsBasicPipeline.call("<imageName>", "<stackName>", "<projName>", "<intTestPort>", endpoints, "<slackChannel>") 
```

#### Docker Compose Pipeline
For this pipeline to work, you must have a `docker-compose-jenkins.yml` in the root of your project. The images must
have `-dev` appended to them and the version must be set to `$GIT_HASH`. For example, ACORN would have the images
`acorn-client-dev:${GIT_HASH}` and `acorn-server-dev:${GIT_HASH}`.
``` 
#!groovy
@Library('lts-basic-pipeline') _

// imageNames: List of images defined in the docker-compose-jenkins.yml i.e. ['acorn-client', 'acorn-server']
// projName: The directory name for the project on the servers for it's docker/config files
// intTestPort: Port of integration test container
// intTestEndpoints: List of integration test endpoints i.e. ['healthcheck/', 'another/example/']
// default values: slackChannel = "lts-jenkins-notifications"

def endpoints = []
def imageNames = []
ltsDockerComposePipeline.call(imageNames, "<stackName>", "<projName>", "<intTestPort>", endpoints, "<slackChannel>") 
```

NOTE: don't exclude the '_' at the end of the import line
