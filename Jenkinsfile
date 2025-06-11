pipeline {
    agent any

    tools {
        maven 'maven-3.9.6'
        jdk 'Java 17'
    }

    environment {
        VPS_HOST = '195.35.22.253'
        VPS_USER = 'root'
        CREDENTIALS_ID = 'vps-ssh-credentials-id-credentialsId'
        JAR_NAME = 'order-0.0.1-SNAPSHOT.jar'
        REMOTE_PATH = "/home/ubuntu/sfa-service/order-service"
        TEMP_PATH = "/home/ubuntu/sfa-service/order-service/temp"
        STARTUP_SCRIPT = "/tmp/orderStartUp.sh"
        WINSCP_PATH = "/root/sfa-service/order-service"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm: [$class: 'GitSCM',
                    branches: [[name: '*/deployjenkins']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/srathore30/order-service.git',
                        credentialsId: 'github-token-pipeline'
                    ]]
                ]
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Add Host Key') {
            steps {
                sh '''
                    mkdir -p ~/.ssh
                    ssh-keyscan -H $VPS_HOST >> ~/.ssh/known_hosts
                '''
            }
        }

        stage('Prepare Start Script') {
            steps {
                sh """
                    echo "Generating startup script..."

                    cat << 'EOF' > ${env.STARTUP_SCRIPT}
#!/bin/bash

echo "[INFO] Checking for running service on port 9092..."
PID=\$(sudo lsof -t -i:9092)

if [ -n "\$PID" ]; then
    echo "[INFO] Found running process with PID: \$PID. Killing it..."
    sudo kill -9 \$PID
    echo "[INFO] Process killed."
else
    echo "[INFO] No process running on port 9092."
fi

echo "[INFO] Starting new JAR..."
nohup java -jar ${env.JAR_NAME} > nohup.out 2>&1 &
echo "[INFO] New service started successfully!"
EOF

                    chmod +x ${env.STARTUP_SCRIPT}
                """
            }
        }

        stage('Deploy to VPS') {
            steps {
                sshagent([env.CREDENTIALS_ID]) {
                    sh """
                        echo "[INFO] Creating temp directory..."
                        ssh ${env.VPS_USER}@${env.VPS_HOST} 'mkdir -p ${TEMP_PATH}'

                        echo "[INFO] Copying JAR to temp..."
                        scp target/${env.JAR_NAME} ${env.VPS_USER}@${env.VPS_HOST}:${TEMP_PATH}/

                        echo "[INFO] Copying startup script..."
                        scp ${env.STARTUP_SCRIPT} ${env.VPS_USER}@${env.VPS_HOST}:${REMOTE_PATH}/

                        echo "[INFO] Deploying on VPS..."
                        ssh ${env.VPS_USER}@${env.VPS_HOST} '
                            set -e
                            cd ${REMOTE_PATH}

                            echo "[INFO] Removing old JAR..."
                            rm -f *.jar

                            echo "[INFO] Copying new JAR from temp to service folder..."
                            cp ${TEMP_PATH}/${JAR_NAME} ${REMOTE_PATH}/

                            echo "[INFO] Cleaning up temp..."
                            rm -rf ${TEMP_PATH}

                            echo "[INFO] Starting service..."
                            chmod +x orderStartUp.sh
                            ./orderStartUp.sh

                            echo "[INFO] Deployment complete."
                        '
                    """
                }
            }
        }

        stage('Upload for WinSCP Access') {
                steps {
                    sshagent(['vps-ssh-credentials-id-credentialsId']) {
                        sh """
                            echo "[INFO] Creating WinSCP folder if needed..."
                            ssh ${env.VPS_USER}@${env.VPS_HOST} 'mkdir -p ${WINSCP_PATH}'

                            echo "[INFO] Uploading final JAR to WinSCP path..."
                            scp target/${env.JAR_NAME} ${env.VPS_USER}@${env.VPS_HOST}:${WINSCP_PATH}/
                        """
                    }
                }
            }
    }
}