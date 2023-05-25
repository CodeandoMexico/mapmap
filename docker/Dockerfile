# Este fichero se usa para generar la imagen de Docker de produccion usando GitHub Actions
# https://github.com/CodeandoMexico/mapmap/blob/main/.github/workflows/build-server-docker-image.yml

FROM docker.io/eclipse-temurin:17-jdk-jammy as build

RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    && rm -rf /var/lib/apt/lists/*

COPY mapmap-server/ ./mapmap-server

RUN mvn -B package --file mapmap-server/pom.xml -Dmaven.test.skip
RUN mv mapmap-server/target/mapmap-server-0.0.1-SNAPSHOT.jar /mapmap-server.jar

FROM docker.io/eclipse-temurin:17-jre-jammy

LABEL org.opencontainers.image.source="https://github.com/codeandomexico/mapmap"

ENV DATABASE_URL jdbc:postgresql://postgres/mapmap
ENV DATABASE_USER postgres
ENV DATABASE_PASSWORD postgres
ENV LOGGIN_LEVEL INFO
ENV CONTEXT_PATH  /

WORKDIR /mapmap-server

COPY --from=build /mapmap-server.jar .

ENTRYPOINT java -jar /mapmap-server/mapmap-server.jar --spring.datasource.url='${DATABASE_URL}' --spring.datasource.username='${DATABASE_USER}' --spring.datasource.password='${DATABASE_PASSWORD}' --logging.level.root='${LOGGIN_LEVEL}' --server.servlet.context-path='${CONTEXT_PATH}'
