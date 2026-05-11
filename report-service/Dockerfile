FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Copy parent POM first for dependency caching
COPY pom.xml parent-pom.xml
COPY report-service/pom.xml .
RUN mvn -f parent-pom.xml dependency:go-offline -pl report-service -am -q 2>/dev/null || true
COPY report-service/src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S xwz && adduser -S xwz -G xwz
COPY --from=build /build/target/*.jar app.jar
USER xwz
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
