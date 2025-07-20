FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
LABEL authors="youhavetrouble"

WORKDIR /app

# Copy Maven wrapper and project files
COPY . .

# Build the JAR file
RUN mvn clean package

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/Inviter.jar Inviter.jar
RUN chmod 755 Inviter.jar

# Run the JAR file
CMD ["java", "-jar", "Inviter.jar", "hostname=0.0.0.0"]
