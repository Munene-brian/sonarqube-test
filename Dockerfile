# Stage 1: Build the application using Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the rest of the code
COPY src ./src

# Build the application (skip tests for speed)
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image using JRE 21
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy only the built JAR file from the build stage
COPY --from=build /app/target/QRCODE-ENGINE-0.0.1-SNAPSHOT.jar app.jar

# Set timezone (if needed)
RUN echo "Africa/Nairobi" > /etc/timezone && \
    ln -sf /usr/share/zoneinfo/Africa/Nairobi /etc/localtime

# Expose the port the application runs on
EXPOSE 62000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]