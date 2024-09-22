# Use the Gradle image to build the project
FROM gradle:jdk17 as build

# Install Gradle and set the environment variables
# I had to use the 7.4.2 version of Gradle to get the build to work
ARG GRADLE_VERSION=7.4.2
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm -rf /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
ENV PATH=${GRADLE_HOME}/bin:${PATH}

COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN ./gradlew clean build --refresh-dependencies

# Use the OpenJDK image to run the application
FROM openjdk:17-alpine
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]