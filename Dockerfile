FROM eclipse-temurin:21-jdk-alpine

RUN apk add typst

WORKDIR /app

COPY ./backend/target/mycv.jar ./app.jar
COPY ./frontend/dist ./static

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]