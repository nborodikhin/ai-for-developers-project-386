# Stage 1: Build frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app
COPY api/generated/@typespec/openapi3/openapi.yaml /api-spec/openapi.yaml
COPY frontend/package*.json ./
RUN npm ci
RUN npx openapi-typescript /api-spec/openapi.yaml -o src/generated/api.ts
COPY frontend/ .
RUN npm run build

# Stage 2: Build backend
FROM eclipse-temurin:17-jdk-alpine AS backend-builder
WORKDIR /workspace
COPY api/generated/@typespec/openapi3/openapi.yaml /api-spec/openapi.yaml
COPY backend/gradlew backend/gradlew.bat ./
COPY backend/gradle ./gradle
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true
COPY backend/src ./src
RUN ./gradlew bootJar --no-daemon -DopenApiSpecPath=/api-spec/openapi.yaml

# Stage 3: Runtime — nginx + JRE in one container
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache nginx curl gettext

COPY --from=frontend-builder /app/dist /usr/share/nginx/html
COPY --from=backend-builder /workspace/build/libs/*.jar /app/backend.jar

COPY nginx.conf /etc/nginx/nginx.conf.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENV PORT=80
EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
