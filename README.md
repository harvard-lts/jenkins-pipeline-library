# Jenkins Pipeline Library
This is a shared library for resuable pipelines 

## How to use library in your project pipeline

### Jenkinsfile
``` 
#!groovy
@Library('lts-basic-pipeline') _

pipeline {
    // projName is the directory name for the project on the servers for it's docker/config files
    // default values: 
    //  registryCredentialsId = "${env.REGISTRY_ID}"
    //  registryUri = 'https://registry.lts.harvard.edu'
    ltsBasicPipeline "<imageName>" "<stackName>" "<projName>" "<slackChannel>" "<registryCredentialsId>" "<registryUri>" 
}
```

NOTE: don't exclude the '_' at the end of the import line
