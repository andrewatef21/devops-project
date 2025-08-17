pipeline {
  agent any
  environment {
    DOCKER_USER = credentials('DOCKER_USER_TEXT')     // We'll map as text env in global props
    DOCKER_PASS = credentials('DOCKER_PASS_TEXT')     // or plain env; see steps below
    IMAGE_REPO  = "docker.io/${DOCKER_USER}/java-hello"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build & Push Image') {
      steps {
        script {
          def tag = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker build -t ${IMAGE_REPO}:${tag} -t ${IMAGE_REPO}:latest .
            docker push  ${IMAGE_REPO}:${tag}
            docker push  ${IMAGE_REPO}:latest
            echo ${tag} > _new_tag.txt
          """
        }
      }
    }
    stage('Bump Kustomize Tag & Push') {
      steps {
        script {
          def newTag = readFile("_new_tag.txt").trim()
          sh """
            sed -i "s/newTag: .*/newTag: ${newTag}/" k8s/base/kustomization.yaml
            git config user.email "ci@example.com"
            git config user.name "ci-bot"
            git add k8s/base/kustomization.yaml
            git commit -m "CI: bump image tag to ${newTag}" || echo "No changes"
            git push origin HEAD:main
          """
        }
      }
    }
  }
  post {
    success { echo "Build OK. ArgoCD will auto-sync." }
    failure { echo "Build failed." }
  }
}
