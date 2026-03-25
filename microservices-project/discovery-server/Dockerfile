FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/discovery-server-0.0.1-SNAPSHOT.jar /app/discovery-server.jar

ENTRYPOINT ["java", "-jar", "/app/discovery-server.jar"]

EXPOSE 8761
