# Build file JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build bỏ qua test để nhanh hơn
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng (Chỉ dùng JRE cho nhẹ)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Mở cổng 8080 cho BE
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]