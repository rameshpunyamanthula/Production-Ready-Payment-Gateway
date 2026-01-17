FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/payment-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000

CMD ["java", "-jar", "app.jar"]
