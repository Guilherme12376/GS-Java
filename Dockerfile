# =====================================================================
# Conecta Alimento API - imagem Docker usada no deploy do Render
# Etapa 1 compila com Maven + JDK 21; etapa 2 roda só com o JRE 21.
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
# limita a memória da JVM ao contêiner (plano gratuito do Render: 512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
COPY --from=build /app/target/conecta-alimento-api.jar app.jar
EXPOSE 8080
# o Render informa a porta na variável PORT (lida em application.properties)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
