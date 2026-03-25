FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/product-service-0.0.1-SNAPSHOT.jar /app/product-service.jar

ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]

EXPOSE 8081
