FROM node:21 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json ./
COPY frontend/yarn.lock ./
RUN yarn install --frozen-lockfile
COPY frontend/ .
RUN yarn build

FROM maven:3.9-eclipse-temurin-21-alpine as backend-build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn clean package

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=backend-build /app/target/mycv.jar app.jar
COPY --from=frontend-build /app/frontend/dist ./static

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]