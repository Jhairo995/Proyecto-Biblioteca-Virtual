# ---------- Etapa 1: compilación ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Las pruebas se ejecutan en el pipeline de CI; aquí solo se empaqueta
RUN ./mvnw -B -q -DskipTests package

# ---------- Etapa 2: imagen de ejecución (liviana, usuario sin privilegios) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/uploads && chown -R app:app /app
COPY --from=build /app/target/*.jar app.jar
USER app
ENV APP_UPLOADS_DIR=/app/uploads
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
