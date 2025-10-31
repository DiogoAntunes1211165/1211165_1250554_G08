pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'maven:3.9.2-eclipse-temurin-17-alpine'
        SONARQUBE_ENV = 'sonarqube'  // nome configurado no Jenkins
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

        stage('Run Unit Tests') {
            steps {
                echo 'Running only unit tests inside Docker container...'
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                mvn -Dtest=pt.psoft.g1.psoftg1.unitTests.**.*Test test
                """
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis (Azure VM)...'
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh """
                    docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                    mvn sonar:sonar \
                        -Dsonar.projectKey=psoft-g1 \
                        -Dsonar.projectName="PSoft G1 Project" \
                        -Dsonar.host.url=http://74.161.33.56:9000 \
                        -Dsonar.login=squ_186e07b99759c0ff10a3f1127bbb2b79ed20a393
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
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

        stage('Run Integration Tests') {
            steps {
                echo 'Running integration tests inside Docker container...'
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                mvn -Dtest=pt.psoft.g1.psoftg1.integrationTests.**.*Test test
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
