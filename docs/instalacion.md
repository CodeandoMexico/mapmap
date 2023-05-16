# Instalación y configuración de MapMap

## Server

La aplicación de servidor es una aplicación Java (Spring Boot).
* Spring Boot 3.0.1
* JDK 17+
* Postgres 15 con la extensión PostGIS 3.3.2

### Para desarrollo

1. Instalar Postgres y PostGIS

Versiones:
   * Postgres 15
   * PostGIS 3.3.2

En este documento no se explica como instalar esto.

1. Crear usuario/s de base de datos

Idealmente se debería crear un usuario que no sea administrador. No es el fin de este documento explicar como realizar esto.

3. Ejecutar Scripts de BBDD

Antes de poder usar la BBDD es necesario crear la base de datos mapmap, y darle permisos al usuario de lectura y escritura.
En la carpeta mapmap-server/scripts-bbdd hay dos scripts de SQL:
    * schema.sql para crear la estructura de BBDD
    * data.sql para insertar unos datos iniciales necesarios para que funcione la aplicación

4. Instalar dependencias de Java

Versiones:
    * Java 17+
    * Maven 3.9.1+

En este documento no se explica como instalar esto.

5. Descargar el código del proyecto

> git clone https://github.com/CodeandoMexico/mapmap.git

6. Configurar la conexión con BBDD

En el fichero mapmap-server/src/main/resources/application.properties se configuran varios aspectos de la aplicación.

La configuracion de BBDD se define con la variables:
    * spring.datasource.url
    * spring.datasource.username
    * spring.datasource.password

7. Correr el proyecto

Es necesario asegurarse de que Postgres está corriendo y se han creado correctamente las tablas y se han insertado los datos de los scripts schema.sql y data.sql.

Desde el raiz del proyecto ejecutar:
> mvn -f mapmap-server/pom.xml spring-boot:run

O desde la carpeta mapmap-server ejecutar:
> mvn spring-boot:run

### Para producción manualmente

1. Instalar Postgres y PostGIS en el servidor
2. Instalar dependencias de Java
3. En la maquina de desarrollo crear el JAR

Desde el raiz del proyecto ejecutar:
> mvn -f mapmap-server/pom.xml package -DskipTests

O desde la carpeta mapmap-server ejecutar:
> mvn package -DskipTests

4. Copiar el JAR al servidor. Este se encuentra en la carpeta

> mapmap-server/target/XXX.jar

5. En el servidor ejecutar

> java -jar XXX.jar

### Para producción usando Docker y Docker Compose

Todos estos pasos son en el servidor de producción

1. Instalar Docker

En este documento no se explica como instalar esto.

2. Crear un fichero con variables de entorno

> touch .env

En la carpeta docker hay un fichero de ejemplo con las variables de entorno que son configurables

DATABASE_URL=jdbc:postgresql://postgres/mapmap
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
LOGGIN_LEVEL=INFO
POSTGRES_PASSWORD=postgres

3. Crear un fichero de Docker Compose

> touch docker-compose.yml

En la carpeta docker hay un fichero de ejemplo de Docker Compose

4. Levantar solo el servicio de BBDD

> docker compose up postgres

Mientras está corriendo, copiar los ficheros SQL al contenedor de Postgres

> docker cp ./schema.sql XXX:/schema.sql
> docker cp ./data.sql XXX:/data.sql

Siendo XXX el ID del contenedor de Postgres. Este se puede obtener ejecutando 'docker ps'.

Una vez copiados los ficheros al contenedor los ejecutamos dentro de la instancia

docker exec -u postgres XXX psql postgres postgres -f schema.sql
docker exec -u postgres XXX psql postgres postgres -f data.sql

Esto dependerá del usuario y contraseña configurado como administrador en Postgres.

Hecho esto, se puede detener el contenedor de Postgres.

5. Levantar todo con Docker Compose

Una vez inicializada la BBDD ya se puede levantar todo el proyecto

> docker compose up
