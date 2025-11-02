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
                echo 'Cloning repository (branch: staging)...'
                git branch: 'staging', url: 'https://github.com/DiogoAntunes1211165/1211165_1250554_G08.git'
            }
        }

        stage('Run Unit Tests + JaCoCo') {
            steps {
                echo 'Running unit tests and generating JaCoCo report...'
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} mvn clean verify
                """
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh """
                    docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                    mvn sonar:sonar \
                        -Dsonar.projectKey=psoft-g1-staging \
                        -Dsonar.projectName="PSoft G1 Project Staging" \
                        -Dsonar.host.url=http://74.161.33.56:9000 \
                        -Dsonar.login=squ_186e07b99759c0ff10a3f1127bbb2b79ed20a393 \
                        -Dsonar.java.coveragePlugin=jacoco \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
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
                echo 'Building Docker image for staging...'
                sh """
                docker build -t psoft-g1:staging .
                """
            }
        }

        stage('Deploy to Staging') {
            steps {
                echo 'Deploying Docker container to staging...'
                sh """
                docker-compose -f docker-compose.staging.yml down --remove-orphans
                docker-compose -f docker-compose.staging.yml up -d --build
                """
            }
        }
    }

    post {
        success {
            echo 'Staging pipeline completed successfully!'
        }
        failure {
            echo 'Staging pipeline failed.'
        }
        always {
            echo 'Pipeline finished.'
        }
    }
}
