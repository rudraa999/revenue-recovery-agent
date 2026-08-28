# Stage 1: Build the Application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# 1. Copy the wrapper and pom first to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Fix line endings from Windows (CRLF) to Linux (LF) AND set execution permissions
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# 2. Download dependencies (this layer is cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline

# 3. Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the Application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]  