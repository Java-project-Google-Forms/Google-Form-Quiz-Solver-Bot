# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY gradle.properties gradle.properties
COPY app/build.gradle.kts app/build.gradle.kts
COPY app/src app/src

RUN chmod +x gradlew && ./gradlew :app:shadowJar --no-daemon \
    -Dorg.gradle.java.installations.auto-download=false

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /workspace/app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
