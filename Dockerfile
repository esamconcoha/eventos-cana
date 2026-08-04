# Build de dos etapas: la imagen final no lleva Maven ni el codigo fuente,
# solo el jar ya compilado. JDK 21 fijo en la imagen para no depender del
# JAVA_HOME del host (ver memoria "build-requiere-jdk21-explicito").

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Se usa el "mvn" de la propia imagen y no el wrapper (mvnw): evita el lio de
# permisos de ejecucion / CRLF que trae un checkout de Windows en un contenedor
# Linux (ver .gitattributes, que ya normaliza mvnw pero no el bit +x).
RUN mvn -q -B dependency:go-offline
COPY src src
RUN mvn -q -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/CANA-0.0.1-SNAPSHOT.jar app.jar

# server.port ya lee ${PORT:8082} (ver application.properties); la plataforma
# de despliegue inyecta PORT y este EXPOSE es solo documentacion.
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
