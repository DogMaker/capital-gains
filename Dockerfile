FROM gradle:jdk17 AS build

WORKDIR /app
COPY --chown=gradle:gradle . /app

RUN gradle build --no-daemon

FROM openjdk:17-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
