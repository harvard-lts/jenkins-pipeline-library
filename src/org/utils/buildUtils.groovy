// src/org/utils/buildUtils.groovy
package org.utils

def devDockerComposeBuild(git_hash) {
    docker.withRegistry(registryUri, registryCredentialsId) {
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml build --no-cache")
        sh("GIT_HASH=${git_hash} docker-compose -f docker-compose-jenkins.yml push")
    }
}

def devDockerComposeTagLatest(image_name, git_hash) {
    sh("docker pull artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash}")
    devImage = docker.image("artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
        devImage.push('latest')
    }
}

def basicImageBuild(image_name, git_hash, environment) {
    def image = docker.build("artifactory.huit.harvard.edu/lts/${image_name}-${environment}:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
      // push the image with hash image
      image.push()
      // then tag with latest
      image.push('latest')
    }
}

def publishProdImage(image_name, git_hash, git_tag) {
    sh("docker pull artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash}")
    sh("docker tag artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash} artifactory.huit.harvard.edu/lts/${image_name}:${git_tag}")
    prodImage = docker.image("artifactory.huit.harvard.edu/lts/${image_name}:${git_tag}")
    docker.withRegistry(registryUri, registryCredentialsId){
        prodImage.push()
    }
}

def publishQAImage(image_name, git_hash) {
    sh("docker pull artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash}")
    sh("docker tag artifactory.huit.harvard.edu/lts/${image_name}-dev:${git_hash} artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash}")
    qaImage = docker.image("artifactory.huit.harvard.edu/lts/${image_name}-qa:${git_hash}")
    docker.withRegistry(registryUri, registryCredentialsId){
        qaImage.push()
        qaImage.push('latest')
    }
}
