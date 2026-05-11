FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Copy parent POM first for dependency caching
COPY pom.xml parent-pom.xml
COPY api-gateway/pom.xml .
RUN mvn -f parent-pom.xml dependency:go-offline -pl api-gateway -am -q 2>/dev/null || true
COPY api-gateway/src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S xwz && adduser -S xwz -G xwz
COPY --from=build /build/target/*.jar app.jar
USER xwz
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
