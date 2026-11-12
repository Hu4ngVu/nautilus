FROM eclipse-temurin:25-jre-noble
LABEL authors="huangnosimp"
WORKDIR /app
COPY build/libs/nautilus-server.jar /opt/app/

ENTRYPOINT ["java", "-jar", "/opt/app/nautilus-server.jar"]