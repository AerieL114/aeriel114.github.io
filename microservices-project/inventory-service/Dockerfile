FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/inventory-service-0.0.1-SNAPSHOT.jar /app/inventory-service.jar

ENTRYPOINT ["java", "-jar", "/app/inventory-service.jar"]

EXPOSE 8083
