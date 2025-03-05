# Start with a base image containing Java runtime
#FROM eclipse-temurin:latest
FROM eclipse-temurin:21-jre as runtime

#Switch to /app directory
WORKDIR /app

# Create a non-root user for security
RUN useradd -m -s /bin/bash qruser
USER qruser

# Add only the application's jar to the container
# Warning: Pipeline builds different images for Sit, Preprod and Prod
#COPY ./target/QRCODE-ENGINE-0.0.1-SNAPSHOT.jar app.jar
COPY --chown=qruser:qruser ./target/QRCODE-ENGINE-0.0.1-SNAPSHOT.jar app.jar

#RUN echo "Africa/Nairobi" > /etc/timezone
ENV TZ=Africa/Nairobi

#Run jar file
#ENTRYPOINT ["java","-jar","app.jar"]
ENTRYPOINT ["java", "-jar", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "app.jar"]