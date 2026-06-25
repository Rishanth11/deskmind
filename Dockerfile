# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
# Copy the actual source code
COPY src ./src

# Package the application (skipping tests to speed up deployment)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the Application
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the dynamic port Render assigns
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]