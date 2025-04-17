# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven build file to the container
COPY /target/inventory-0.0.1-SNAPSHOT.jar auth.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]