// src/org/utils/buildUtils.groovy
package org.utils

def devDockerComposeBuild(git_hash) {
    docker.withRegistry(registryUri, registryCredentialsId) {
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml build --no-cache")
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml push")
    }
}