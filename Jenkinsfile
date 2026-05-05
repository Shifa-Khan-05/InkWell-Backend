pipeline {
    agent any

    environment {
        BACKEND_REPO = 'https://github.com/Shifa-Khan-05/InkWell-Backend.git'
        FRONTEND_REPO = 'https://github.com/Shifa-Khan-05/InkWell-Frontend.git'
        COMPOSE_FILE = 'docker-compose.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    dir('backend') {
                        git url: "${BACKEND_REPO}", branch: 'main'
                    }
                    dir('frontend') {
                        git url: "${FRONTEND_REPO}", branch: 'main'
                    }
                }
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    // Start by building backend images
                    dir('backend') {
                        sh 'docker-compose build'
                    }
                    // Build frontend image manually since it might not be in the backend compose file
                    dir('frontend') {
                        sh 'docker build -t inkwell-frontend:latest .'
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    dir('backend') {
                        // Stop old containers
                        sh 'docker-compose down'
                        
                        // Stop frontend if running
                        sh 'docker stop inkwell-frontend-container || true'
                        sh 'docker rm inkwell-frontend-container || true'
                        
                        // Deploy new containers (Backend services)
                        sh 'docker-compose up -d'
                    }
                    
                    // Deploy frontend container
                    dir('frontend') {
                        sh 'docker run -d --name inkwell-frontend-container -p 80:80 --network inkwell-network inkwell-frontend:latest'
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
