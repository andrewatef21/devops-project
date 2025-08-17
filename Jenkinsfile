pipeline {
  agent any
  options { skipDefaultCheckout() }
  environment {
    IMAGE_REPO = "docker.io/andrewatef/devops-project"
    APP_NAME   = "devops-app"
    APP_PORT   = "8080"                 // host port
    HEALTH_URL = "http://localhost:8080/"
    // Change to your actual creds ID if different:
    DOCKER_CREDS_ID = "dockerhub-creds"
    STATE_FILE = "/var/jenkins_home/last_deployed_image.txt"
  }

  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build & Push Docker image') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: "${DOCKER_CREDS_ID}",
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

echo "Pushing $BUILD_TAG and $LATEST_TAG"
docker push "$BUILD_TAG"
docker push "$LATEST_TAG"
'''
        }
      }
    }

    stage('Capture previous image (for rollback)') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
# Save currently running image (if any) to STATE_FILE
if docker inspect "${APP_NAME}" >/dev/null 2>&1; then
  CURR_IMG=$(docker inspect -f '{{.Config.Image}}' "${APP_NAME}")
  echo "$CURR_IMG" | tee "${STATE_FILE}"
  echo "Saved previous image: $CURR_IMG"
else
  echo "none" | tee "${STATE_FILE}"
  echo "No previous container/image found."
fi
'''
      }
    }

    stage('Deploy new container') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
NEW_IMG="${IMAGE_REPO}:${BUILD_NUMBER}"

# Stop/remove any existing container
docker rm -f "${APP_NAME}" 2>/dev/null || true

# Run new container (expects port 80 inside; adjust if your app differs)
docker run -d --name "${APP_NAME}" --restart=always -p ${APP_PORT}:80 "${NEW_IMG}"

docker ps --filter "name=${APP_NAME}"
'''
      }
    }

    stage('Smoke test') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
echo "Waiting for app to become healthy at ${HEALTH_URL} ..."
for i in {1..30}; do
  if curl -fsS --max-time 2 "${HEALTH_URL}" >/dev/null; then
    echo "Smoke test passed."
    exit 0
  fi
  sleep 2
done
echo "Smoke test FAILED after 60s."
exit 1
'''
      }
    }
  }

  post {
    failure {
      // Auto-rollback if we had a previous image
      sh '''#!/usr/bin/env bash
set -euo pipefail
if [ -f "${STATE_FILE}" ]; then
  PREV_IMG=$(cat "${STATE_FILE}")
  if [ "$PREV_IMG" != "none" ]; then
    echo "Rolling back to $PREV_IMG ..."
    docker rm -f "${APP_NAME}" 2>/dev/null || true
    docker run -d --name "${APP_NAME}" --restart=always -p ${APP_PORT}:80 "$PREV_IMG"
    docker ps --filter "name=${APP_NAME}"
  else
    echo "No previous image to roll back to."
  fi
else
  echo "No state file found; cannot rollback."
fi
'''
    }
    always {
      sh '''#!/usr/bin/env bash
set -euo pipefail
docker system prune -f || true
'''
    }
  }
}
