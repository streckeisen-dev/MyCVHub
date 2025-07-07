FROM eclipse-temurin:21-jdk-alpine

RUN apk update \
    && apk add typst \
    && apk add font-awesome-free \
    && apk cache clean

WORKDIR /app

COPY ./backend/target/mycv.jar ./app.jar
COPY ./frontend/dist ./static

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]