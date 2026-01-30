# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user (optional but good practice, though staying root for simple file permissions on volumes might be easier for a CLI tool. 
# Sticking to simple setup for now as this is a local utility)

# Copy JAR
COPY --from=build /app/target/school-events-organizer-1.0.0-SNAPSHOT.jar app.jar

# Create output and data directories
RUN mkdir -p output

# Expected Environment Variables:
# GMAIL_USERNAME, GMAIL_PASSWORD, GEMINI_API_KEY, AI_ENABLED, DB_URL, GOOGLE_CREDENTIALS_JSON, DRIVE_FOLDER_ID, UI_PASSWORD

ENTRYPOINT ["java", "-jar", "app.jar"]
