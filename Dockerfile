# Use a multi-stage build to reduce the final image size
FROM eclipse-temurin:17-jre-jammy as builder
WORKDIR /app
COPY . /app
RUN ./gradlew assemble

# Create the final image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]