FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/java0715-0.1.0-SNAPSHOT.jar /app/java0715.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/java0715.jar"]
