![Logo Codeando México](/recursos/imagenes/logo-cmx.svg#gh-light-mode-only)
![Logo Codeando México](/recursos/imagenes/logo-cmx-blanco.svg#gh-dark-mode-only)

[![website](https://img.shields.io/badge/website-CodeandoMexico-00D88E.svg)](http://www.codeandomexico.org/)
[![slack](https://img.shields.io/badge/slack-CodeandoMexico-EC0E4F.svg)](http://slack.codeandomexico.org/)


# MapMap

MapMap es un kit de código abierto para el mapeo de rutas de transporte público.

## Índice

- [Guía y plantilla para proyectos de Codeando México](#guía-y-plantilla-para-proyectos-de-codeando-méxico)
  - [Índice](#índice)
  - [Nombre del repositorio](#nombre-del-repositorio)
  - [[Archivo] Readme](#archivo-readme)
    - [[Sección] Créditos](#sección-créditos)
    - [[Sección] Referencias](#sección-referencias)
    - [[Sección] Código de conducta](#sección-código-de-conducta)
  - [[Archivo] Licencia de uso](#archivo-licencia-de-uso)
  - [[Archivo] Cómo contribuir al proyecto](#archivo-cómo-contribuir-al-proyecto)
  - [[Archivo] Hacklog](#archivo-hacklog)
  - [[Carpeta] Docs](#carpeta-docs)
  - [[Carpeta] Recursos](#carpeta-recursos)

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

* Android 9 (sdk 28)
* Java 11
* Android Studio (recomendado)

## Aplicación de servidor

La aplicación de servidor sirve para:
* Recibir los datos recabados mediante la aplicación móvil
* Visualizar las rutas en un mapa web
* Descargar las rutas en formatos Shapefile y CSV

### Tecnología

* Java 11 en adelante
* Spring Boot 3

### Metodología de mapeo

<strong>MapMap</strong> fue utilizada en el ejercicio del <a href="https://mapaton.org" target="_blank">Mapatón Ciudadano</a>, esté mapeo colaborativo realizado en el ciudad de Xalapa dió como resultado una propuesta metodológica para realizar el trazado de rutas de transporte público en <strong>cualquier ciudad que quiera implementarlo</strong>, se divide en 3 apartados:<br><br>
1. <strong>Previo al Trazado</strong><br>
 <code>1.1.</code> Tener identificado brigadistas (personas que harán el trazado).<br>
 <code>1.2.</code> Tener un primer acercamiento con checadores y choferes, desde los puntos más importantes que concentran rutas.<br>
 <code>1.3.</code> Identificar los puntos de inicio y fin.<br>
 <code>1.4.</code> Mapear las rutas en papel (fieldpapers.org), en la medida de lo posible.<br>
 <code>1.5.</code> Verificar si en los periodos de tiempo en lo que se quiere realizar el trazado se van a presentar eventos extraordinarios.<br>
 <code>1.6.</code> Identificar las zonas geográficas.<br>
 <code>1.7.</code> Organizar brigadas de voluntarios por zonas y rutas.<br>
 <code>1.8.</code> Asignar líderes a la brigadas de voluntarios.<br>
 <code>1.9.</code> Probar MapMap en una ruta seleccionada antes de iniciar el proceso.<br>
 <code>1.10.</code> Capacitar a las brigadas de voluntarios (ver manual de usuario).<br>
 <code>1.11.</code> dentificar horarios y días en los que hay disponibilidad el servicio de transporte público.<br><br>
2. <strong>Durante el Trazado</strong><br>
 <code>2.1.</code> Organizar las brigadas de voluntarios por parejas.<br>
 <code>2.2.</code> Cubrir todas las rutas.<br>
 <code>2.3.</code> Realizar y repetir el trazado de ruta.<br>
 <code>2.4.</code> Los líderes de brigadas deben estar en contacto con sus brigadas de voluntarios.<br>
 <code>2.5.</code> Reportar el progreso del trazado por parte de los brigadistas voluntarios a los líderes de brigada.<br><br>
3. <strong>Posterior al Trazado</strong><br>
 <code>3.1.</code> Recopilar la información resultado del trazado en el repositorio.<br>
 <code>3.2.</code> Las brigadas de voluntarios deben informar a sus líderes que concluyeron el proceso de trazado.<br>
 <code>3.3.</code> Los líderes deben llevar un registro de la zonas y rutas a su cargo que han sido trazadas por completo.<br>
 <code>3.4.</code> La información deben ser verificada y contrastada con la cartografía de la zona geográfica que fue trazada.<br><br>
> Existe una versión extendida de la metodología la cual se puede ver en: <a href="https://mapaton.org">mapaton.org</a>.


### Autores:

#### Codeando Xalapa

* [Rolando Drouaillet Pumarino](https://github.com/rdrouaillet)
* [Juan Manuel Becerril del Toro](https://github.com/jmbecerril)
* Elizabeth Montenegro Ñeco
* Elías Martín Sánchez Jímenez
* [Abraham Toriz Cruz](https://github.com/categulario)

#### Codeando México

* [Óscar Hernández](https://github.com/oxcar)


### Licencia
Licencia MIT [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


### Agradecimiento

Agradecemos a todos los miembros que pertenecen a la comunidad de <a href="http://codeandoxalapa.org/">Codeando Xalapa</a> por contribuir en este proyecto. También agradecemos al proyecto [TransitWand](https://github.com/conveyal/transit-wand) por haber creado la aplicación móvil que sirvió como base para construir <strong>MapMap</strong>.

---

Creado con ❤️ por la comunidad de [Codeando México](http://www.codeandomexico.org).
