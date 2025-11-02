pipeline {
    agent any

    environment {
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
                echo 'Cloning repository (branch: main)...'
                git branch: 'main', url: 'https://github.com/DiogoAntunes1211165/1211165_1250554_G08.git'
            }
        }

        stage('Build JAR') {
            steps {
                echo 'Building project JAR (skipping tests)...'
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} mvn clean package -DskipTests
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image for production...'
                sh 'docker build -t psoft-g1:prod .'
            }
        }

        stage('Deploy to Production') {
            steps {
                echo 'Deploying Docker container to production...'
                sh """
                docker-compose -f docker-compose.production.yml down --remove-orphans
                docker-compose -f docker-compose.production.yml up -d --build
                """
            }
        }


    post {
        success {
            echo 'Production deployment completed successfully!'
        }
        failure {
            echo 'Production deployment failed.'
        }
    }
}
