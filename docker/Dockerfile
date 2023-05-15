FROM eclipse-temurin:17

LABEL org.opencontainers.image.source="https://github.com/codeandomexico/mapmap"

ENV DATABASE_URL jdbc:postgresql://postgres/mapmap
ENV DATABASE_USER postgres
ENV DATABASE_PASSWORD postgres
ENV LOGGIN_LEVEL INFO

WORKDIR /mapmap-server

COPY mapmap-server.jar .

ENTRYPOINT java -jar /mapmap-server/mapmap-server.jar --spring.datasource.url='${DATABASE_URL}' --spring.datasource.username='${DATABASE_USER}' --spring.datasource.password='${DATABASE_PASSWORD}' --logging.level.root='${LOGGIN_LEVEL}'
