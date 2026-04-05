FROM node:20-alpine AS builder
WORKDIR /app

COPY api/generated/@typespec/openapi3/openapi.yaml /api-spec/openapi.yaml

COPY frontend/package*.json ./
RUN npm ci

RUN npx openapi-typescript /api-spec/openapi.yaml -o src/generated/api.ts

COPY frontend/ .
RUN npm run build

FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
