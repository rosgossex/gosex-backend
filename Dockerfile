FROM gradle:8.7-jdk21-jammy AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml

RUN gradle dependencies --no-daemon

COPY src ./src

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN useradd -m appuser && chown -R appuser /app
USER appuser

EXPOSE 6969
ENTRYPOINT ["java", "-jar", "app.jar"]
