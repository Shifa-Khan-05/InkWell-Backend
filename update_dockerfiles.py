import os

services = [
    'eureka-server', 'api-gateway', 'auth-service', 'post-service',
    'comment-service', 'media-service', 'newsletter-service', 
    'notification-service', 'payment-service', 'taxonomy-service',
    'website-controller', 'admin-server'
]

dockerfile_content = """FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
"""

for s in services:
    path = os.path.join(s, 'Dockerfile')
    if os.path.exists(path):
        with open(path, 'w') as f:
            f.write(dockerfile_content)
        print(f'Updated {path}')
