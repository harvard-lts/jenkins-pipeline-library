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

    // trial is optional and only goes to dev
   stage('Build and Publish trial image') {
      when {
            branch 'trial'
        }
      steps {
        echo 'Building and Pushing docker image to the registry...'
        script {
            if (GIT_TAG != "") {
              echo "$GIT_TAG"
              def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}:$GIT_TAG")
              docker.withRegistry(registryUri, registryCredentialsId){
                customImage.push()
              }
            } else {
                  echo "$GIT_HASH"
                  def devImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-dev:$GIT_HASH")
                  docker.withRegistry(registryUri, registryCredentialsId){
                    // push the dev with hash image
                    devImage.push()
                    // then tag with latest
                    devImage.push('latest')
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
                  TESTS_PASSED = sh (script: "ssh -t -t ${env.DEV_SERVER} 'curl -k https://${env.CLOUD_DEV}:<port>/apps/healthcheck'",
                  returnStdout: true).trim()
                  echo "${TESTS_PASSED}"
                  if (!TESTS_PASSED.contains("\"num_failed\": 0") || !TESTS_PASSED_2.contains("\"num_failed\": 0")){
                    error "Dev trial integration tests did not pass"
                  } else {
                    echo "All test passed!"
                  }
                }
              }
          }
      }
    }
   stage('Build and Publish dev image') {
      when {
            branch 'main'
        }
      steps {
        echo 'Building and Pushing docker image to the registry...'
        script {
            if (GIT_TAG != "") {
              echo "$GIT_TAG"
              def customImage = docker.build("registry.lts.harvard.edu/lts/${imageName}:$GIT_TAG")
              docker.withRegistry(registryUri, registryCredentialsId){
                customImage.push()
              }
            } else {
                  echo "$GIT_HASH"
                  def devImage = docker.build("registry.lts.harvard.edu/lts/${imageName}-dev:$GIT_HASH")
                  docker.withRegistry(registryUri, registryCredentialsId){
                    // push the dev with hash image
                    devImage.push()
                    // then tag with latest
                    devImage.push('latest')
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
                  TESTS_PASSED = sh (script: "ssh -t -t ${env.DEV_SERVER} 'curl -k https://${env.CLOUD_DEV}:<port>/apps/healthcheck'",
                  returnStdout: true).trim()
                  echo "${TESTS_PASSED}"
                  if (!TESTS_PASSED.contains("\"num_failed\": 0") || !TESTS_PASSED_2.contains("\"num_failed\": 0")){
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
              echo "Already pushed tagged image in dev deploy"
            } else {
                  echo "$GIT_HASH"
                  sh("docker pull registry.lts.harvard.edu/lts/${imageName}-dev:$GIT_HASH")
                  sh("docker tag registry.lts.harvard.edu/lts/${imageName}-dev:$GIT_HASH registry.lts.harvard.edu/lts/${imageName}-qa:$GIT_HASH")
                  qaImage = docker.image("registry.lts.harvard.edu/lts/${imageName}-qa:$GIT_HASH")
                  docker.withRegistry(registryUri, registryCredentialsId){
                    qaImage.push()
                    qaImage.push('latest')
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
                  TESTS_PASSED = sh (script: "ssh -t -t ${env.QA_SERVER} 'curl -k https://${env.CLOUD_QA}:<port>/apps/healthcheck'",
                  returnStdout: true).trim()
                  echo "${TESTS_PASSED}"
                  if (!TESTS_PASSED.contains("\"num_failed\": 0") || !TESTS_PASSED_2.contains("\"num_failed\": 0")){
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
  stage('Slack Demo') {
      steps {
          echo "This is for a slack message demo"
    }
  }
   post {
        fixed {
            script {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "@jessi_jassal", color: "##77caed", message: "Build Fixed: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            }
        }
        failure {
            script {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "@jessi_jassal", color: "danger", message: "Build Failed: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            }
        }
        success {
            script {
                    // Specify your project channel here. Feel free to add/remove states that are relevant to your project (i.e. fixed, failure,...)
                    slackSend channel: "@jessi_jassal", color: "good", message: "Build Succeeded: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
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
