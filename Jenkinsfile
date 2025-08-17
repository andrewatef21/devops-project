pipeline {
  agent any

  environment {
    DOCKER_USER = credentials('docker-username-id') // optional if you use withCredentials below
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Push Docker image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''#!/usr/bin/env bash
set -euo pipefail

echo "Docker version:"
docker --version

echo "Logging in to Docker registry…"
echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

IMAGE="andrewatef/devops-project:${BUILD_NUMBER}"

echo "Building image $IMAGE"
docker build -t "$IMAGE" .

echo "Pushing image $IMAGE"
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
