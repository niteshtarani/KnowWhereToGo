# Use a multi-stage build to reduce the final image size
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY . /app
RUN chmod +x gradlew

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx2g"

RUN ./gradlew clean shadowJar --no-daemon --max-workers=1 --stacktrace --debug

# Verify build output
RUN find . -name "*.jar" -type f

EXPOSE 8080

CMD ["java", "-jar", "build/libs/*-all.jar"]