pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Checkout the branch Jenkins is building
                checkout scm
            }
        }

        stage('Build (skip tests)') {
            steps {
                script {
                    if (isUnix()) {
                        // Use the wrapper if present, fall back to system mvn
                        sh './mvnw -DskipTests clean package || mvn -DskipTests clean package'
                    } else {
                        // Windows agent: use mvnw.cmd or mvn
                        bat "mvnw.cmd -DskipTests clean package || mvn -DskipTests clean package"
                    }
                }
            }
        }

        stage('Run') {
            steps {
                script {
                    if (isUnix()) {
                        // Start the jar in background with UTF-8 file.encoding, capture pid and show a short tail of log
                        sh 'nohup java -Dfile.encoding=UTF-8 -jar target/*.jar > app.log 2>&1 & echo $! > app.pid'
                        sh 'sleep 5 || true'
                        sh 'echo "--- App log (last 50 lines) ---" && tail -n 50 app.log || true'
                    } else {
                        // Windows: locate the jar in target and start it using PowerShell (works around wildcard expansion issues)
                        bat 'powershell -NoProfile -Command "$jar=(Get-ChildItem -Path target -Filter \"*.jar\" | Select-Object -First 1).FullName; if ($jar) { Start-Process -NoNewWindow -FilePath java -ArgumentList \"-Dfile.encoding=UTF-8\", \"-jar\", $jar -RedirectStandardOutput app.log -RedirectStandardError app.log } else { Write-Host \"No jar found in target\"; exit 1 }"'
                        bat 'timeout /T 5 /NOBREAK'
                        // Show a short tail of the log using PowerShell if available
                        bat 'powershell -NoProfile -Command "if (Test-Path \"app.log\") { Get-Content app.log -Tail 50 } else { Write-Host \"app.log not found yet\" }"'
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Best-effort cleanup on Unix agents (kill the PID we stored). Windows cleanup is omitted to avoid killing unrelated Java processes.
                if (isUnix()) {
                    sh 'if [ -f app.pid ]; then kill $(cat app.pid) || true; rm -f app.pid || true; fi'
                } else {
                    echo 'Windows agent: pipeline finished (no automatic Java process kill to avoid killing unrelated processes)'
                }
                echo 'Pipeline finished'
            }
        }
    }
}

