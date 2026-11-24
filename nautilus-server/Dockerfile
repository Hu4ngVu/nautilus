FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
COPY lombok.config lombok.config
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/nautilus-server.jar .
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "nautilus-server.jar"]