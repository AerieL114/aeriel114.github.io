FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/order-service-0.0.1-SNAPSHOT.jar /app/order-service.jar

ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]

EXPOSE 8082
