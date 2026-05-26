pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'DEPLOY_DIR', defaultValue: '/opt/xinyu', description: '部署根目录')
        string(name: 'SERVICE_NAME', defaultValue: 'xinyu-backend', description: 'systemd 后端服务名')
    }

    environment {
        BACKEND_DIR = 'backend'
        FRONTEND_DIR = 'leading'
        JAR_NAME = 'cross-border-ecommerce-backend-1.0.0.jar'
        MAVEN_HOME = '/opt/maven'
        PATH+MAVEN = '/opt/maven/bin'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir(env.BACKEND_DIR) {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir(env.FRONTEND_DIR) {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    set -e

                    mkdir -p "${DEPLOY_DIR}/app" "${DEPLOY_DIR}/web" "${DEPLOY_DIR}/backup" "${DEPLOY_DIR}/config"

                    if [ -f "${DEPLOY_DIR}/app/${JAR_NAME}" ]; then
                      cp "${DEPLOY_DIR}/app/${JAR_NAME}" "${DEPLOY_DIR}/backup/${JAR_NAME}.$(date +%Y%m%d%H%M%S)"
                    fi

                    cp "${BACKEND_DIR}/target/${JAR_NAME}" "${DEPLOY_DIR}/app/${JAR_NAME}"

                    rm -rf "${DEPLOY_DIR}/web"/*
                    cp -r "${FRONTEND_DIR}/dist"/* "${DEPLOY_DIR}/web/"

                    sudo systemctl restart "${SERVICE_NAME}"
                    sudo systemctl status "${SERVICE_NAME}" --no-pager
                '''
            }
        }
    }

    post {
        success {
            echo 'XinYu 部署完成'
        }
        failure {
            echo 'XinYu 部署失败，请查看 Jenkins Console Output'
        }
    }
}
