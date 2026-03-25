FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/notification-service-0.0.1-SNAPSHOT.jar /app/notification-service.jar

ENTRYPOINT ["java", "-jar", "/app/notification-service.jar"]

EXPOSE 8084
