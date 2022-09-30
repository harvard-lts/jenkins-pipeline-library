#!/usr/bin/env groovy

def call(List imageNames, String stackName, String projName, String slackChannel = "lts-jenkins-notifications", String intTestPort, List intTestEndpoints) {
    pipeline {
      agent any
      stages {
        stage('Configure') {
          when { anyOf { branch 'main'; branch 'trial' } }
          steps {
            script {
                GIT_TAG = sh(returnStdout: true, script: "git tag | head -1").trim()
                echo "$GIT_TAG"
                GIT_HASH = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
                echo "$GIT_HASH"
            }
          }
        }
      }
      environment {
            imageNames = []
            stackName = ''
            // projName is the directory name for the project on the servers for it's docker/config files
            projName = ''
            slackChannel=''
            registryCredentialsId = "${env.REGISTRY_ID}"
            registryUri = 'https://registry.lts.harvard.edu'
      }
    }
}
