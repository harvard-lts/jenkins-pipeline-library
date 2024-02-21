#!/usr/bin/env groovy

def call(String imageName, String stackName, String projName, String intTestPort, List intTestEndpoints, String slackChannel = "lts-jenkins-notifications") {

  pipeline {

  agent any
  stages {
    stage('Configure') {
      when { anyOf { branch 'main'; branch 'trial'; buildingTag() } }
      steps {
        script {
          GIT_TAG = env.BRANCH_NAME
          echo "$GIT_TAG"
          GIT_HASH = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          echo "$GIT_HASH"
          buildUtils = new org.utils.buildUtils()
       }
      }
    }
    
    stage('Publish Prod Image'){
      when {
          buildingTag()
      }
      steps {
        script {
          echo "$GIT_HASH"
          echo "$GIT_TAG"
          buildUtils.publishProdImage(imageName, GIT_HASH, GIT_TAG)
        }
      }
    }
    // trial is optional and only goes to dev
   stage('Build and Publish trial image') {
      when {
        allOf {
          branch 'trial';
          not { buildingTag() }
        }
      }
      steps {
        echo 'Building and Pushing docker image to the registry...'
        script {
            echo "$GIT_HASH"
            buildUtils.basicImageBuild(imageName, GIT_HASH, "dev")
        }
      }
   }
   stage('Build and Publish dev image') {
      when {
          allOf {
            branch 'main';
            not { buildingTag() }
          }
        }
      steps {
        echo 'Building and Pushing docker image to the registry...'
        script {
            echo "$GIT_HASH"
            buildUtils.basicImageBuild(imageName, GIT_HASH, "dev")
        }
      }
    }
    stage('Publish main qa image') {
      when {
           allOf {
            branch 'main';
            not { buildingTag() }
          }
        }
      steps {
        echo 'Pushing docker image to the registry...'
        echo "$GIT_TAG"
        script {
              echo "$GIT_HASH"
              buildUtils.publishQAImage(imageName, GIT_HASH)
        }
      }
    }
  }
   post {
        fixed {
            script {
                if(env.BRANCH_NAME == "main" || env.BRANCH_NAME == "trial") {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "#${slackChannel}", color: "##77caed", message: "Build Fixed: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
            }
        }
        failure {
            script {
                if(env.BRANCH_NAME == "main" || env.BRANCH_NAME == "trial") {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "#${slackChannel}", color: "danger", message: "Build Failed: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
            }
        }
        success {
            script {
                if(env.BRANCH_NAME == "main" || env.BRANCH_NAME == "trial") {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "#${slackChannel}", color: "good", message: "Build Succeeded: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
            }
        }
    }
   environment {
    imageName = ''
    stackName = ''
    // projName is the directory name for the project on the servers for it's docker/config files
    projName = ''
    slackChannel=''
    registryCredentialsId = "${env.REGISTRY_ID}"
    registryUri = 'https://registry.lts.harvard.edu'
    artUri = 'https://artifactory.huit.harvard.edu'
    artCredentialsId = "{env.ARTIFACTORY_ID}"
   }
 }
}
