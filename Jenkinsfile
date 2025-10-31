pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'maven:3.9.2-eclipse-temurin-17-alpine'
        SONARQUBE_ENV = 'sonarqube'
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
                echo "Checking out branch: ${env.BRANCH_NAME}"
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/DiogoAntunes1211165/1211165_1250554_G08.git'
            }
        }

        stage('Set Environment') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'staging') {
                        env.SPRING_PROFILE = 'staging'
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.staging.yml'
                        env.SONAR_PROJECT_KEY = 'psoft-g1-staging'
                    } else if (env.BRANCH_NAME == 'dev') {
                        env.SPRING_PROFILE = 'dev'
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.dev.yml'
                        env.SONAR_PROJECT_KEY = 'psoft-g1-dev'
                    } else {
                        error "Unsupported branch: ${env.BRANCH_NAME}. Only 'dev' and 'staging' are allowed."
                    }
                    echo "Using profile: ${env.SPRING_PROFILE}"
                }
            }
        }

        stage('Run Unit Tests + JaCoCo') {
            steps {
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                mvn clean verify
                """
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh """
                    docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                    mvn sonar:sonar \
                        -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
                        -Dsonar.projectName="PSoft G1 Project" \
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
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build JAR') {
            steps {
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} mvn clean package -DskipTests
                """
            }
        }

        stage('Run Integration Tests') {
            steps {
                sh """
                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} \
                mvn -Dtest=pt.psoft.g1.psoftg1.integrationTests.**.*Test test
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t psoft-g1:${env.BRANCH_NAME} .
                """
            }
        }

        stage('Deploy') {
            steps {
                sh """
                docker-compose -f ${env.DOCKER_COMPOSE_FILE} down --remove-orphans
                docker-compose -f ${env.DOCKER_COMPOSE_FILE} up -d --build
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully for branch ${env.BRANCH_NAME}!"
        }
        failure {
            echo "❌ Pipeline failed for branch ${env.BRANCH_NAME}."
        }
        always {
            echo "Pipeline finished."
        }
    }
}
