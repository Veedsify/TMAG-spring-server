# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Maven wrapper and POM first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre

WORKDIR /app

# Application Settings
ENV APP_ENV=debug \
    SERVER_PORT=8080 \
    APP_FRONTEND_URL=http://localhost:3000 \
    APP_HOST=http://localhost:8080

# Database Configuration
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/travel-medicine-advisory \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD="" \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Security
ENV JWT_SECRET="" \
    APP_API_KEY=""

# Mail
ENV SPRING_MAIL_HOST=smtp.resend.com \
    SPRING_MAIL_PORT=587 \
    SPRING_MAIL_USERNAME=resend \
    SPRING_MAIL_PASSWORD=""

# Redis
ENV REDIS_HOST=localhost \
    REDIS_PORT=6379

# Storage
ENV APP_STORAGE_PATH=storage/upload \
    APP_STORAGE_BASE_URL=http://localhost:8080/storage/upload

# Flutterwave Payment
ENV FLUTTERWAVE_PUBLIC_KEY="" \
    FLUTTERWAVE_SECRET_KEY="" \
    FLUTTERWAVE_ENCRYPTION_KEY="" \
    FLUTTERWAVE_CALLBACK_URL=http://localhost:3000/payment/callback \
    FLUTTERWAVE_WEBHOOK_URL=http://localhost:8080/api/v1/payments/webhook/flutterwave

# Create storage directory
RUN mkdir -p /app/storage/upload

COPY --from=build /app/target/Server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
