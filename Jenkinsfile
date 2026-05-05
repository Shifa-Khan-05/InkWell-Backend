pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6' // Ensure a Maven tool is configured in Jenkins under Global Tool Configuration with this name
    }

    environment {
        BACKEND_REPO = 'https://github.com/Shifa-Khan-05/InkWell-Backend.git'
        DOCKER_USERNAME = 'shifakhan05'
        // Make sure to add Docker Hub credentials in Jenkins with ID 'dockerhub-creds'
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
                // Build all JAR files locally without running tests
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    // Login to Docker Hub
                    sh 'echo $DOCKER_CREDS_PSW | docker login -u $DOCKER_CREDS_USR --password-stdin'

                    def services = [
                        'eureka-server', 'api-gateway', 'auth-service', 'post-service',
                        'comment-service', 'media-service', 'newsletter-service', 
                        'notification-service', 'payment-service', 'taxonomy-service',
                        'website-controller', 'admin-server'
                    ]

                    for (service in services) {
                        def imageName = "${DOCKER_USERNAME}/inkwell-${service}:latest"
                        // Build image using the Dockerfile inside each service directory
                        sh "docker build -t ${imageName} ./${service}"
                        // Push to Docker Hub
                        sh "docker push ${imageName}"
                    }
                }
            }
        }
    }

    post {
        always {
            // Logout and clean up
            sh 'docker logout'
            cleanWs()
        }
    }
}
