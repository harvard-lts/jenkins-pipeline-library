// src/org/utils/buildUtils.groovy
package org.utils

def devDockerComposeBuild(git_hash) {
    docker.withRegistry(registryUri, registryCredentialsId) {
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml build --no-cache")
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml push")
    }
}

def basicImageBuild(image_name, git_hash, environment) {
    def image = docker.build("registry.lts.harvard.edu/lts/${image_name}-${environment}:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
      // push the image with hash image
      image.push()
      // then tag with latest
      image.push('latest')
    }
}

def publishProdImage(image_name, git_hash, git_tag) {
    sh("docker pull registry.lts.harvard.edu/lts/${image_name}-qa:${git_hash}")
    sh("docker tag registry.lts.harvard.edu/lts/${image_name}-qa:${git_hash} registry.lts.harvard.edu/lts/${image_name}:${git_tag}")
    prodImage = docker.image("registry.lts.harvard.edu/lts/${image_name}:${git_tag}")
    docker.withRegistry(registryUri, registryCredentialsId){
        prodImage.push()
    }
}

def publishQAImage(image_name, git_hash) {
    sh("docker pull registry.lts.harvard.edu/lts/${image_name}-dev:${git_hash}")
    sh("docker tag registry.lts.harvard.edu/lts/${image_name}-dev:${git_hash} registry.lts.harvard.edu/lts/${image_name}-qa:${git_hash}")
    qaImage = docker.image("registry.lts.harvard.edu/lts/${image_name}-qa:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
        qaImage.push()
        qaImage.push('latest')
    }
}

def runIntegrationTests(int_test_endpoints, server, hostname, int_test_port){
    sshagent(credentials : ['hgl_svcupd']) {
            for(int i = 0; i < int_test_endpoints.size(); i++){
                String endpoint = int_test_endpoints.get(i)
                TESTS_PASSED = sh (script: "ssh -t -t ${server} 'curl -k https://${hostname}:${int_test_port}/${endpoint}'",
                returnStdout: true).trim()
                echo "${TESTS_PASSED}"
            if (!TESTS_PASSED.contains("\"num_failed\": 0")){
                error "Integration tests did not pass for endpoint: ${endpoint}"
            } else {
                echo "All test passed for endpoint: ${endpoint}!"
            }
        }
    }
}