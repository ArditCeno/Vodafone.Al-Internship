FROM node:20-alpine AS frontend-builder
WORKDIR /app
COPY src/Tobi2/frontend/package*.json ./
RUN npm ci
COPY src/Tobi2/frontend/ ./
RUN npm run build

FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
COPY --from=frontend-builder /app/dist ./src/main/resources/static
RUN cp src/main/resources/application-tobi2.yml.example src/main/resources/application-tobi2.yml && \
    mvn clean package -P tobi2 -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache fontconfig ttf-dejavu
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=tobi2"]
