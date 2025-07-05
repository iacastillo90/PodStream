FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/PodStreamBack.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]