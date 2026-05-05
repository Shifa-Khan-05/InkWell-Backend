import os

services = [d for d in os.listdir('.') if os.path.isdir(d) and os.path.exists(os.path.join(d, 'pom.xml'))]

for s in services:
    dockerfile_content = """FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
"""
    with open(os.path.join(s, 'Dockerfile'), 'w') as f:
        f.write(dockerfile_content)
    
    prod_yml_content = """spring:
  config:
    import: optional:file:../.env[.properties]
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://eureka-server:8761/eureka/}
"""
    # some services like eureka, gateway might not need datasource, but we can put it there or customize it.
    res_dir = os.path.join(s, 'src', 'main', 'resources')
    if os.path.exists(res_dir):
        with open(os.path.join(res_dir, 'application-prod.yml'), 'w') as f:
            f.write(prod_yml_content)

print('Done creating Dockerfiles and application-prod.yml')
