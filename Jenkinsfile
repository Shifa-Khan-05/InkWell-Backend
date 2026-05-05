pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
    }

    environment {
        BACKEND_REPO = 'https://github.com/Shifa-Khan-05/InkWell-Backend.git'
        DOCKER_USERNAME = 'shifakhan05'
        DOCKER_CREDS = credentials('dockerhub-creds')
    }

    stages {
        stage('Checkout') {
            steps {
                git url: "${BACKEND_REPO}", branch: 'main'
            }
        }

        stage('Build Backend JARs') {
    steps {
        sh '''
        cd auth-service && mvn clean package -DskipTests && cd ..
        cd api-gateway && mvn clean package -DskipTests && cd ..
        cd eureka-server && mvn clean package -DskipTests && cd ..
        cd post-service && mvn clean package -DskipTests && cd ..
        cd comment-service && mvn clean package -DskipTests && cd ..
        cd media-service && mvn clean package -DskipTests && cd ..
        cd newsletter-service && mvn clean package -DskipTests && cd ..
        cd notification-service && mvn clean package -DskipTests && cd ..
        cd payment-service && mvn clean package -DskipTests && cd ..
        cd taxonomy-service && mvn clean package -DskipTests && cd ..
        cd website-controller && mvn clean package -DskipTests && cd ..
        cd admin-server && mvn clean package -DskipTests && cd ..
        '''
    }
}

        stage('Build & Push Docker Images') {
            steps {
                script {
                    sh 'echo $DOCKER_CREDS_PSW | docker login -u $DOCKER_CREDS_USR --password-stdin'

                    def services = [
                        'eureka-server', 'api-gateway', 'auth-service', 'post-service',
                        'comment-service', 'media-service', 'newsletter-service',
                        'notification-service', 'payment-service', 'taxonomy-service',
                        'website-controller', 'admin-server'
                    ]

                    for (service in services) {
                        def imageName = "${DOCKER_USERNAME}/inkwell-${service}:latest"
                        sh "docker build -t ${imageName} ./${service}"
                        sh "docker push ${imageName}"
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
            cleanWs()
        }
    }
}