![Logo Codeando México](/recursos/imagenes/logo-cmx.svg#gh-light-mode-only)
![Logo Codeando México](/recursos/imagenes/logo-cmx-blanco.svg#gh-dark-mode-only)

[![website](https://img.shields.io/badge/website-CodeandoMexico-00D88E.svg)](http://www.codeandomexico.org/)
[![slack](https://img.shields.io/badge/slack-CodeandoMexico-EC0E4F.svg)](http://slack.codeandomexico.org/)


# MapMap

MapMap es un kit de código abierto para el mapeo de rutas de transporte público.

## Índice

- [MapMap](#mapmap)
  - [Índice](#índice)
  - [Problemática](#problemática)
  - [Descripción](#descripción)
  - [Aplicación móvil](#aplicación-móvil)
    - [Tecnología](#tecnología)
    - [Instalación de la app móvil](#instalación-de-la-app-móvil)
  - [Aplicación de servidor](#aplicación-de-servidor)
    - [Tecnología](#tecnología-1)
    - [Instalación de la aplicación de servidor](#instalación-de-la-aplicación-de-servidor)
  - [Metodología](#metodología)
  - [Autores:](#autores)
    - [Codeando Xalapa](#codeando-xalapa)
    - [Codeando México](#codeando-méxico)
  - [Licencia](#licencia)
  - [Agradecimiento](#agradecimiento)

## Problemática

En un gran porcentaje de las ciudades latinoamericanas el transporte público se brinda a través de concesiones por medio del sector privado, esto implica un desconocimiento del número de concesiones que transitan por las ciudades, los itinerarios son irregulares así como las rutas por donde circulan los camiones. El no conocer las rutas genera un serio problema para la movilidad de las personas y la administración pública al no lograr gestionar adecuadamente el transporte público de una ciudad.

## Descripción

MapMap es un kit de mapeo de transporte público. El proyecto consiste en:
* Una aplicación móvil para Android
* Una aplicación de servidor

Este proyecto es una versión modernizada del proyecto [MapMap](https://github.com/codeandoxalapa/mapmap) originalmente desarrollado por Codeando Xalapa en el marco del [Mapatón Xalapa 2016](https://mapaton.org/mapaton-ciudadano-xalapa/). En este repositorio está una nueva versión con algunos ajustes, mejoras y corrección de errores.

Con el tiempo el proyecto MapMap se ha usado en mapatones en diferentes partes de México (Veracruz, Michoacán, Baja California Sur) y otros países (Ecuador, Colombia, República Dominicana), y en el proceso se identificaron mejoras y correcciones que era necesario realizar, además de la necesidad de actualizar el stack tecnológico del proyecto para facilitar su mantenimiento y mejorar su estabilidad.

Para más información del proyecto original consultar el [respositorio original](https://github.com/codeandoxalapa/mapmap) del proyecto.

## Aplicación móvil

Mediante la aplicación móvil es posible capturar trazos georeferenciados sin la necesidad de consumir datos del dispositivo móvil. La información que se recolecta a través de la aplicación es la siguiente:

- Conteo de pasajeros (subida y bajada de usuarios en las paradas).
- Puntos que marcan las paradas por donde se detiene el transporte público.
- Línea de trazo que identifica una ruta.
- Tiempo inicial y final del recorrido.
- Fotografía del transporte público que realiza la ruta.

### Tecnología

* Android 5.0 (sdk 21)
* Java 8+

Se recomienda Android Studio como IDE para el desarrollo.

### Instalación de la app móvil

La [última versión de la aplicación móvil](https://github.com/CodeandoMexico/mapmap/blob/bea4fab344db3058b896c842796012149ad7263a/releases/app-debug.apk) se encuentra en la carpeta releases.

La aplicación se creó para el Android SDK 21, y en versiones modernas de Android al intentar instalarla puede dar aviso de que no es recomendable instalarla. Esta app apunta directamente a los servidores de Codeando México.

Si realizas una instalación de tu propia instancia de MapMap Server, es necesario configurar la App Android para apuntar a tu servidor y recompilar el APK.

Esto lo puedes realizar en el fichero:

mapmap-app/app/src/main/java/CaptureService.java en la linea 38.

## Aplicación de servidor

La aplicación de servidor sirve para:
* Recibir los datos recabados mediante la aplicación móvil
* Visualizar las rutas en un mapa web
* Descargar las rutas en formatos Shapefile y CSV

### Tecnología

* Java 17+
* Spring Boot 3

Se recomienda IntelliJ IDEA o Visual Studio Code (Metals) para el desarrollo.

### Instalación de la aplicación de servidor

El proyecto se puede instalar:

* De manera manual
* Usando Docker

Más información en el [documento de instalación](docs/instalacion.md)

## Metodología

[Documento de metodología](docs/metodologia.md)

## Autores:

### Codeando Xalapa

* [Rolando Drouaillet Pumarino](https://github.com/rdrouaillet)
* [Juan Manuel Becerril del Toro](https://github.com/jmbecerril)
* Elizabeth Montenegro Ñeco
* Elías Martín Sánchez Jímenez
* [Abraham Toriz Cruz](https://github.com/categulario)

### Codeando México

* [Óscar Hernández](https://github.com/oxcar)


## Licencia

El proyecto consiste de dos partes, las cuales están publicadas con dos licencias diferentes. Cada proyecto tiene la licencia en su carpeta:
* Una [aplicación móvil Android](https://github.com/CodeandoMexico/mapmap/tree/master/mapmap-app), con Licencia MIT. [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
* Una [aplicación web](https://github.com/CodeandoMexico/mapmap/tree/master/mapmap-server), con licencia GNU Affero General Public License v3.0. [![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

Si estás planeando reutilizar la aplicación web, es necesario que publiques el código bajo la misma licencia, GPL Affero 3, en un repositorio de acceso público y mantengas los avisos de autoría, además de indicar los cambios realizados al código existente.

Te recomendamos leas a detalle la licencia. Puedes contactarnos si tienes alguna duda al respecto.

## Agradecimiento

Agradecemos a todos los miembros que pertenecen a la comunidad de <a href="http://codeandoxalapa.org/">Codeando Xalapa</a> por contribuir en este proyecto. También agradecemos al proyecto [TransitWand](https://github.com/conveyal/transit-wand) por haber creado la aplicación móvil que sirvió como base para construir <strong>MapMap</strong>.

---

Creado con 💜💙💚💛❤️ por la comunidad de [Codeando México](http://www.codeandomexico.org).
