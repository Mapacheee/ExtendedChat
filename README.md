# ExtendedChat

Plugin de chat para Paper y Folia 1.21.8, con formatos por permisos, chat de staff,
mensajes privados, filtros de spam y enlaces, y colores HEX/gradientes por jugador.

## Dependencias

- Java 25, requerido por la publicación actual de MapacheeeLib 1.0.2.
- MapacheeeLib 1.0.2 instalado en el servidor y publicado en Maven local para compilar.
- PlaceholderAPI 2.12.2 opcional, para placeholders en formatos y la expansión
  `%extendedchat_name_color%`, `%extendedchat_message_color%` y sus variantes `_legacy`.

Winter, Cloud, Configurate y las dependencias compartidas las proporciona MapacheeeLib.
Los procesadores de anotaciones se declaran directamente desde los módulos originales
de Winter para generar los registros de servicios, listeners, comandos y configuraciones.
FastStats se resuelve desde `https://repo.faststats.dev/releases` como dependencia de
MapacheeeLib; ExtendedChat no necesita declararlo directamente.

## Compilación y pruebas

Publica primero MapacheeeLib con `gradlew.bat publishToMavenLocal` desde su proyecto.
Después, desde ExtendedChat:

```powershell
.\gradlew.bat clean build
```

El JAR se genera en `build/libs/ExtendedChat-1.0.1-SNAPSHOT.jar`.
La tarea `build` ejecuta pruebas de recarga de filtros y persistencia concurrente de colores.

## Comandos

- `/extendedchat reload`: recarga `config.yml` y `messages.yml`; requiere `extendedchat.admin`.
- `/staffchat` o `/sc`: activa el chat de staff; acepta también un mensaje directo.
- `/msg`, `/w`, `/tell`, `/reply`, `/r`: mensajes privados.
- `/color` y `/color reset`: menú de colores y restablecimiento.

Los archivos de configuración conservan sus comentarios mediante Configurate.
Los colores se guardan en `colors.yml`, incluso si la función estaba desactivada al arrancar.
Los filtros toman una instantánea inmutable y se actualizan al recargar la configuración.

## Folia

Las tareas que acceden a jugadores usan sus entity schedulers. Los placeholders del
chat se evalúan en la región del jugador y la espera desde el evento asíncrono está
limitada a dos segundos. No se usan `@RepeatingTask` ni `@ScheduledAt` de Winter.

La validación en un servidor debe cubrir el arranque con y sin PlaceholderAPI,
`/extendedchat reload`, colores HEX/gradientes y mensajes entre jugadores en regiones distintas.
