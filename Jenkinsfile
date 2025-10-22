pipeline {
    agent any

    options {
        skipDefaultCheckout(true) // Vamos fazer checkout manual
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
                // Assumindo que o JAR fica em target/*.jar
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully on branch: dev'
        }
        failure {
            echo 'Build failed.'
        }
        always {
            echo 'Pipeline finished.'
        }
    }
}
