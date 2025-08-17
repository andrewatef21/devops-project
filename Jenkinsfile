pipeline {
  agent any
  environment {
    IMAGE_REPO = "docker.io/andrewatef/java-hello"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build JAR with Maven (in Docker)') {
      steps {
        sh '''
          docker run --rm -v "$PWD":/app -w /app \
            maven:3.9.6-eclipse-temurin-17 \
            mvn -B -DskipTests package
        '''
      }
    }

    stage('Build Docker image') {
      steps {
        sh '''
          TAG="${BUILD_NUMBER}"
          docker build -t "${IMAGE_REPO}:${TAG}" -t "${IMAGE_REPO}:latest" .
        '''
      }
    }

    stage('Push to Docker Hub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-hub',
                      usernameVariable: 'DOCKER_USER',
                      passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push "${IMAGE_REPO}:${BUILD_NUMBER}"
            docker push "${IMAGE_REPO}:latest"
            docker logout || true
          '''
        }
      }
    }
  }
  post {
    always { archiveArtifacts artifacts: 'target/*.jar', onlyIfSuccessful: false }
  }
}
