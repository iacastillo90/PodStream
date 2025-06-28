FROM openjdk:17-jdk-slim
WORKDIR /app
# Construir el proyecto localmente antes de ejecutar docker-compose
COPY build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
