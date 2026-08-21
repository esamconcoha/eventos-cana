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

# Antes de arrancar el JVM se imprime el limite de memoria REAL del contenedor,
# leido del cgroup. Es la forma de saber si la maquina de Fly quedo en 256 MB o
# en 1 GB sin depender de lo que diga el dashboard (memory vs memory_mb en
# fly.toml se contradecian y ganaba el valor chico).
#   -XX:MaxRAMPercentage=75  el heap usa el 75% de la RAM del contenedor; el
#                            default es 25%, que desperdicia la mayor parte.
#   -Xlog:gc+init=info       imprime que tamano de heap eligio el JVM.
#   -Duser.timezone          zona horaria del negocio. SIN esto el contenedor
#                            corre en UTC y todo LocalDate.now()/LocalDateTime.now()
#                            devuelve la fecha UTC: estando en UTC-6, de 18:00 a
#                            23:59 locales el servidor ya esta en el dia siguiente.
#                            Eso hacia que el dashboard contara las entregas de
#                            manana como "de hoy" y que las validaciones de
#                            "fecha no anterior a hoy" rechazaran pedidos del dia.
#                            Se usa el flag del JVM y no la variable TZ porque
#                            Java resuelve el nombre de zona contra su propio
#                            tzdb (lib/tzdb.dat) y asi no depende de que la
#                            imagen alpine traiga el paquete tzdata.
# Se usa la forma shell + "exec" para que java reemplace al sh y siga recibiendo
# los SIGINT/SIGTERM que manda el init de Fly.
ENTRYPOINT echo "[ARRANQUE] limite de memoria del contenedor: $(cat /sys/fs/cgroup/memory.max 2>/dev/null || cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo desconocido) bytes" ; \
    free -m 2>/dev/null ; \
    exec java -Duser.timezone=America/Guatemala -XX:MaxRAMPercentage=75.0 -Xlog:gc+init=info -jar app.jar
