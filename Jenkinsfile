pipeline {

  agent any
  stages {
    stage('Configure') {
      when { anyOf { branch 'main'; branch 'trial' } }
      steps {
        script {
          GIT_TAG = sh(returnStdout: true, script: "git tag | head -1").trim()
          echo "${GIT_TAG}"
          echo "$GIT_TAG"
          GIT_HASH = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          echo "$GIT_HASH"
       }
      }
    }
    stage('Build image') {
      when { anyOf { branch 'main'; branch 'trial' } }
      steps {
        echo 'Building'
        sh 'docker build -t registry.lts.harvard.edu/lts/${imageName} .'
      }
    }

    // trial is optional and only goes to dev
    stage('Publish trial image') {
      when {
            branch 'trial'
        }
      steps {
        echo 'Pushing docker image to the registry...'
        echo "$GIT_TAG"
        script {
            if (GIT_TAG != "") {
                echo "$GIT_TAG"
                docker.withRegistry(registryUri, registryCredentialsId){
                def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}:$GIT_TAG")
                customImage.push()
                }
            } else {
                    echo "$GIT_HASH"
                    docker.withRegistry(registryUri, registryCredentialsId){
                    // this says build but its really just using the build from above and tagging it
                    def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-snapshot:$GIT_HASH")
                    customImage.push()
                    def devImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-snapshot:dev")
                    devImage.push()
                    }
            }
        }
      }
    }
    stage('TrialDevDeploy') {
      when {
          branch 'trial'
        }
      steps {
          echo "Deploying to dev"
          script {
              if (GIT_TAG != "") {
                  echo "$GIT_TAG"
                  sshagent(credentials : ['hgl_svcupd']) {
                      sh "ssh -t -t ${env.DEV_SERVER} '${env.STACK_COMMAND} ${env.HOME}${projName}${env.DOCKER} ${stackName}'"
                  }
              } else {
                      echo "$GIT_HASH"
                      sshagent(credentials : ['hgl_svcupd']) {
                      sh "ssh -t -t ${env.DEV_SERVER} '${env.STACK_COMMAND} ${env.HOME}${projName}${env.DOCKER} ${stackName}'"
                  }
              }
          }
      }
    }
    stage('TrialDevIntegrationTest') {
      when {
          branch 'trial'
        }
      steps {
          echo "Running integration tests on dev"
          script {
              sshagent(credentials : ['hgl_svcupd']) {
                script{
                  TESTS_PASSED = sh (script: "ssh -t -t ${env.DEV_SERVER} 'curl -k https://${env.CLOUD_DEV}:<itest-container-port>/apps/healthcheck'",
                  returnStdout: true).trim()
                  echo "${TESTS_PASSED}"
                  if (!TESTS_PASSED.contains("\"num_failed\": 0")){
                    error "Dev trial integration tests did not pass"
                  } else {
                    echo "All test passed!"
                  }
                }
              }
          }
      }
    }
    stage('Publish main dev image') {
      when {
            branch 'main'
        }
      steps {
        echo 'Pushing docker image to the registry...'
        echo "$GIT_TAG"
        script {
            if (GIT_TAG != "") {
                echo "$GIT_TAG"
                docker.withRegistry(registryUri, registryCredentialsId){
                def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}:$GIT_TAG")
                customImage.push()
                }
            } else {
                    echo "$GIT_HASH"
                    docker.withRegistry(registryUri, registryCredentialsId){
                    def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-snapshot:$GIT_HASH")
                    customImage.push()
                    def devImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-snapshot:dev")
                    devImage.push()
                    }
            }
        }
      }
    }
    stage('MainDevDeploy') {
      when {
          branch 'main'
        }
      steps {
          echo "Deploying to dev"
          script {
              if (GIT_TAG != "") {
                  echo "$GIT_TAG"
                  sshagent(credentials : ['hgl_svcupd']) {
                      sh "ssh -t -t ${env.DEV_SERVER} '${env.STACK_COMMAND} ${env.HOME}${projName}${env.DOCKER} ${stackName}'"
                  }
              } else {
                      echo "$GIT_HASH"
                      sshagent(credentials : ['hgl_svcupd']) {
                      sh "ssh -t -t ${env.DEV_SERVER} '${env.STACK_COMMAND} ${env.HOME}${projName}${env.DOCKER} ${stackName}'"
                  }
              }
          }
      }
    }
    stage('MainDevIntegrationTest') {
      when {
          branch 'main'
        }
      steps {
          echo "Running integration tests on dev"
          script {
              sshagent(credentials : ['hgl_svcupd']) {
                script{
                  // TODO: Handle multiple curl commands more elegantly
                  TESTS_PASSED = sh (script: "ssh -t -t ${env.DEV_SERVER} 'curl -k https://${env.CLOUD_DEV}:<itest-container-port>/apps/healthcheck'",
                  returnStdout: true).trim()
                  echo "${TESTS_PASSED}"
                  if (!TESTS_PASSED.contains("\"num_failed\": 0")){
                    error "Dev main integration tests did not pass"
                  } else {
                    echo "All test passed!"
                  }
                }
              }
          }
      }
    }
    stage('Publish main qa image') {
      when {
            branch 'main'
        }
      steps {
        echo 'Pushing docker image to the registry...'
        echo "$GIT_TAG"
        script {
            if (GIT_TAG != "") {
                echo "$GIT_TAG"
                docker.withRegistry(registryUri, registryCredentialsId){
                def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}:$GIT_TAG")
                customImage.push()
                }
            } else {
                    echo "$GIT_HASH"
                    docker.withRegistry(registryUri, registryCredentialsId){
                    def qaImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-snapshot:qa")
                    qaImage.push()
                    }
            }
        }
      }
    }
    stage('MainQADeploy') {
      when {
          branch 'main'
        }
      steps {
          echo "Deploying to qa"
          script {
              if (GIT_TAG != "") {
                  echo "$GIT_TAG"
                  sshagent(credentials : ['qatest']) {
                      sh "ssh -t -t ${env.QA_SERVER} '${env.STACK_COMMAND} ${env.HOME}${projName}${env.DOCKER} ${stackName}'"
                  }
              } else {
                      echo "$GIT_HASH"
                      sshagent(credentials : ['qatest']) {
                      sh "ssh -t -t ${env.QA_SERVER} '${env.STACK_COMMAND} ${env.HOME}${projName}${env.DOCKER} ${stackName}'"
                  }
              }
          }
      }
    }
    stage('MainQAIntegrationTest') {
      when {
          branch 'main'
        }
      steps {
          echo "Running integration tests on QA"
          script {
              sshagent(credentials : ['qatest']) {
                script{
                  TESTS_PASSED = sh (script: "ssh -t -t ${env.QA_SERVER} 'curl -k https://${env.CLOUD_QA}:<itest-container-port>/apps/healthcheck'",
                  returnStdout: true).trim()
                  echo "${TESTS_PASSED}"
                  if (!TESTS_PASSED.contains("\"num_failed\": 0")){
                    error "QA main integration tests did not pass"
                  } else {
                    echo "All test passed!"
                  }
                }
              }
          }
      }
    }
   }
   post {
        fixed {
            script {
                if(env.BRANCH_NAME == "main" || env.BRANCH_NAME == "trial") {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "#<project-channel>", color: "##77caed", message: "Build Fixed: ${env.JOB_NAME} ${env.BUILD_NUMBER}"
                }
            }
        }
        failure {
            script {
                if(env.BRANCH_NAME == "main" || env.BRANCH_NAME == "trial") {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "#<project-channel>", color: "danger", message: "Build Failed: ${env.JOB_NAME} ${env.BUILD_NUMBER}"
                }
            }
        }
        success {
            script {
                if(env.BRANCH_NAME == "main" || env.BRANCH_NAME == "trial") {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "#<project-channel>", color: "good", message: "Build Succeeded: ${env.JOB_NAME} ${env.BUILD_NUMBER}"
                }
            }
        }
    }
   environment {
    imageName = ''
    stackName = ''
    // projName is the directory name for the project on the servers for it's docker/config files
    projName = ''
    registryCredentialsId = "${env.REGISTRY_ID}"
    registryUri = 'https://registry.lts.harvard.edu'
   }
 }
