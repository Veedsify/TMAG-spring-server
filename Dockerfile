# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Runtime stage ----
# No GCP/Vertex credentials in the image. On Cloud Run/GKE use an attached service account;
# elsewhere mount a key at runtime (e.g. GOOGLE_APPLICATION_CREDENTIALS + read-only volume).
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN mkdir -p /app/storage/upload

COPY --from=build /app/target/Server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]