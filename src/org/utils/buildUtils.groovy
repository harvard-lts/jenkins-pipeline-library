// src/org/utils/buildUtils.groovy
package org.utils

def devDockerComposeBuild(git_hash) {
    docker.withRegistry(registryUri, registryCredentialsId) {
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml build --no-cache")
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml push")
    }
    docker.withRegistry(artUri, artCredentialsId) {
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml build --no-cache")
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml push")
    }
}

def devDockerComposeTagLatest(image_name, git_hash) {
    sh("docker pull registry.lts.harvard.edu/lts/${image_name}-dev:${git_hash}")
    devImage = docker.image("registry.lts.harvard.edu/lts/${image_name}-dev:${git_hash}")
    sh("docker pull artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash}")
    devArtImage = docker.image("artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
        devImage.push('latest')
    }
    docker.withRegistry(artUri, artCredentialsId){
        devArtImage.push('latest')
    }
}

def basicImageBuild(image_name, git_hash, environment) {
    def image = docker.build("registry.lts.harvard.edu/lts/${image_name}-${environment}:${git_hash}")
    def artImage = docker.build("artifactory.huit.harvard.edu/lts/${image_name}-${environment}:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
      // push the image with hash image
      image.push()
      // then tag with latest
      image.push('latest')
    }
    echo 'trying to do artifactory'
    docker.withRegistry(artUri, artCredentialsId){
      // push the image with hash image
      artImage.push()
      // then tag with latest
      artImage.push('latest')
    }
}

def publishProdImage(image_name, git_hash, git_tag) {
    sh("docker pull registry.lts.harvard.edu/lts/${image_name}-qa:${git_hash}")
    sh("docker tag registry.lts.harvard.edu/lts/${image_name}-qa:${git_hash} registry.lts.harvard.edu/lts/${image_name}:${git_tag}")
    prodImage = docker.image("registry.lts.harvard.edu/lts/${image_name}:${git_tag}")
    docker.withRegistry(registryUri, registryCredentialsId){
        prodImage.push()
    }
    sh("docker pull artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash}")
    sh("docker tag artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash} artifactory.huit.harvard.edu/lts/${image_name}:${git_tag}")
    prodArtImage = docker.image("artifactory.huit.harvard.edu/lts/${image_name}:${git_tag}")
    docker.withRegistry(artUri, artCredentialsId){
        prodArtImage.push()
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
    sh("docker pull artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash}")
    sh("docker tag artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash} artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash}")
    qaArtImage = docker.image("artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash}")
    docker.withRegistry(artUri, artCredentialsId){
        qaArtImage.push()
        qaArtImage.push('latest')
    }
}

def runIntegrationTests(agent, int_test_endpoints, server, hostname, int_test_port){
    sshagent(credentials : [agent]) {
            sleep 60;
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
