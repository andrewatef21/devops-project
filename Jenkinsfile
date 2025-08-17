pipeline {
  agent any

  environment {
    IMAGE_REPO = "docker.io/andrewatef/java-hello"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Push Docker image') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-hub',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh '''
            set -euxo pipefail
            TAG="${BUILD_NUMBER}"

            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker build -t "${IMAGE_REPO}:${TAG}" -t "${IMAGE_REPO}:latest" .
            docker push "${IMAGE_REPO}:${TAG}"
            docker push "${IMAGE_REPO}:latest"
            docker logout || true
          '''
        }
      }
    }
  }

  post {
    always { sh 'docker system prune -f || true' }
  }
}
