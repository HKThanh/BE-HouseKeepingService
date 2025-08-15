# Build stage
FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app

# Copy gradle configuration files first for better caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (this layer gets cached if build files don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM openjdk:17-slim
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Environment variables for database and Redis
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/house_keeping_v3
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=123456
ENV SPRING_DATA_REDIS_HOST=redis
ENV SPRING_DATA_REDIS_PORT=6379
ENV JWT_SECRET=81474ce734be3de9043102e50fa88519a6f3bc67d75e7efded4602f33062fb40

# Expose the Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]