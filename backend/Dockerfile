FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Copy OpenAPI spec (needed by openapi-generator at build time)
COPY api/generated/@typespec/openapi3/openapi.yaml /api-spec/openapi.yaml

# Copy Gradle wrapper and build files
COPY backend/gradlew backend/gradlew.bat ./
COPY backend/gradle ./gradle
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# Copy source and build
COPY backend/src ./src
RUN ./gradlew bootJar --no-daemon -DopenApiSpecPath=/api-spec/openapi.yaml

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
