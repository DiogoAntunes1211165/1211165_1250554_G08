pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
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
                echo 'Building Maven project and generating JAR (skipping tests)...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Archive Artifact') {
            steps {
                echo 'Archiving generated JAR...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Run Docker Compose') {
            steps {
                echo 'Stopping any existing containers...'
                sh 'docker-compose -f docker-compose.dev.yml down --remove-orphans'

                echo 'Starting Docker container for dev environment...'
                sh 'docker-compose -f docker-compose.dev.yml up -d --build'
            }
        }
    }

    post {
        success {
            echo '✅ Build completed successfully on branch: dev'
        }
        failure {
            echo '❌ Build failed.'
        }
        always {
            echo 'Pipeline finished.'
        }
    }
}
