pipeline {
  agent any
  options { skipDefaultCheckout() }
  environment {
    IMAGE_REPO = "docker.io/andrewatef/devops-project"
    APP_NAME   = "devops-app"
    APP_PORT   = "8080"  // host port to expose
  }
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Build & Push Docker image') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'dockerhub-creds',     // if your ID is 'docker-hub', change here
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh '''#!/usr/bin/env bash
set -euo pipefail
rm -rf ~/.docker || true
printf "%s" "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

BUILD_TAG="${IMAGE_REPO}:${BUILD_NUMBER}"
LATEST_TAG="${IMAGE_REPO}:latest"

echo "Building $BUILD_TAG"
docker build -t "$BUILD_TAG" .

echo "Tag latest -> $LATEST_TAG"
docker tag "$BUILD_TAG" "$LATEST_TAG"

echo "Push $BUILD_TAG and $LATEST_TAG"
docker push "$BUILD_TAG"
docker push "$LATEST_TAG"
'''
        }
      }
    }
    stage('Deploy to EC2') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
IMAGE="${IMAGE_REPO}:${BUILD_NUMBER}"

# Stop old container if any
docker rm -f "${APP_NAME}" 2>/dev/null || true

# Run new container: host 8080 -> container 80 (Nginx)
docker run -d --name "${APP_NAME}" --restart=always -p ${APP_PORT}:80 "${IMAGE}"

docker ps --filter "name=${APP_NAME}"
'''
      }
    }
  }
  post { always { sh 'docker system prune -f || true' } }
}
