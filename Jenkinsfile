pipeline {
    agent any

    environment {
        // Imagem Docker que contém Maven + JDK 17
        DOCKER_IMAGE = 'maven:3.9.2-eclipse-temurin-17-alpine'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning workspace...'
                cleanWs()
            }
        }

        stage('Checkout') {
            steps {
                echo 'Cloning repository (branch: dev)...'
                git branch: 'dev', url: 'https://github.com/DiogoAntunes1211165/1211165_1250554_G08.git'
            }
        }

        stage('Build JAR') {
            steps {
                echo 'Building Maven project inside Docker container...'
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} mvn clean package -DskipTests
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image for the app inside Docker container...'
                sh """
                docker build -t psoft-g1:latest .
                """
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying Docker container...'
                sh """
                docker-compose -f docker-compose.dev.yml down --remove-orphans
                docker-compose -f docker-compose.dev.yml up -d --build
                """
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
        always {
            echo 'Pipeline finished.'
        }
    }
}
