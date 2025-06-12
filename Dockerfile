# Use a multi-stage build to reduce the final image size
FROM openjdk:21
RUN microdnf install findutils
WORKDIR /app
COPY . /app
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx2g"
RUN chmod +x gradlew
RUN ./gradlew shadowJar --no-daemon
RUN find . -name "*.jar" -type f
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/default-0.1-all.jar"]