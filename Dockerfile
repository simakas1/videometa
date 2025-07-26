FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /opt/app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x ./mvnw && \
    ./mvnw dependency:go-offline -B --no-transfer-progress

COPY ./src ./src

RUN ./mvnw clean install -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl && \
    addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

WORKDIR /opt/app

COPY --from=builder --chown=appuser:appuser /opt/app/target/videometa*.jar /opt/app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]