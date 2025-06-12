# Use a multi-stage build to reduce the final image size
FROM eclipse-temurin:17-jre-jammy as builder
WORKDIR /app
COPY . /app
RUN ./gradlew shadowJar

# Create the final image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar default-0.1-all.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/default-0.1-all.jar"]