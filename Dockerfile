FROM node:22-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine as backend-build

WORKDIR /app/backend

COPY backend/target/mycv.jar app.jar
RUN chmod +x app.jar
COPY --from=frontend-build /app/frontend/dist ./static

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]