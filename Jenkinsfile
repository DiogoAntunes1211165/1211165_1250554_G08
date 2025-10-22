pipeline {
    agent any


    options {
        skipDefaultCheckout(true) // We'll manually checkout the desired branch
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository (branch: dev)...'
                git branch: 'dev', url: 'https://github.com/DiogoAntunes1211165/1211165_1250554_G08.git'
            }
        }

        stage('Clean') {
            steps {
                echo 'Cleaning workspace...'
                cleanWs()
            }
        }

        stage('Build') {
            steps {
                echo 'Building Maven project (skipping tests)...'
                sh 'mvn clean package -DskipTests'
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
