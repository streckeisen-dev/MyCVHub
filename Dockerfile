FROM eclipse-temurin:26-jdk-alpine

RUN apk update \
    && apk add typst \
    && apk add font-awesome \
    && apk cache clean

WORKDIR /app

COPY ./backend/target/mycv.jar ./app.jar
COPY ./frontend/dist ./static

RUN addgroup -S nonroot \
    && adduser -S nonroot -G nonroot \
    && install -d -o nonroot -g nonroot /app/logs
USER nonroot

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
