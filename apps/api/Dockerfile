# syntax=docker/dockerfile:1

# ---------- Stage 1: build the jar with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Download dependencies first: this layer is cached until pom.xml changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: small runtime image (JRE only) ----------
FROM eclipse-temurin:21-jre-alpine

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /data/media && chown -R app:app /data

WORKDIR /app
COPY --from=build /workspace/target/app.jar app.jar

USER app

ENV SERVER_PORT=8080 \
    STORAGE_LOCAL_ROOT=/data/media \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
VOLUME ["/data/media"]

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
