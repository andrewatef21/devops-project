pipeline {
  agent any
  options { skipDefaultCheckout() } // cleaner logs
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build & Push Docker image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''#!/usr/bin/env bash
set -euo pipefail

echo "Docker version:"
docker --version

printf "%s" "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

IMAGE="docker.io/${DOCKER_USER}/devops-project:${BUILD_NUMBER}"
echo "Building $IMAGE"
docker build -t "$IMAGE" .

echo "Pushing $IMAGE"
docker push "$IMAGE"
'''
        }
      }
      post {
        always {
          sh '''#!/usr/bin/env bash
set -euo pipefail
docker system prune -f || true
'''
        }
      }
    }
  }
}
