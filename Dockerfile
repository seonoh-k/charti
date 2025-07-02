# 1. 빌드 단계
FROM eclipse-temurin:17-jdk as build
WORKDIR /app
COPY .gradle .gradle
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# 2. 런타임 단계
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
