# Use a lightweight Java 21 image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled jar file into the container
COPY target/*.jar app.jar

# Tell Docker how to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]