# ====== Build stage ======
FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app

# Copy các file Gradle trước để cache dependency
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# CẤP QUYỀN EXECUTE CHO gradlew (fix lỗi Permission denied)
RUN chmod +x gradlew

# Download dependencies (layer này được cache nếu build file không đổi)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build ứng dụng
RUN ./gradlew bootJar --no-daemon -x test
# hoặc: RUN ./gradlew build --no-daemon -x test (tuỳ bạn dùng task nào)

# ====== Runtime stage ======
FROM eclipse-temurin:17-jdk-alpine AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
