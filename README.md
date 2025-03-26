# TimeSync - Plugin para Minecraft

Un plugin que sincroniza el ciclo día/noche de Minecraft con la hora real de una zona horaria configurable.

---

## Características
- Sincroniza el tiempo del juego con la hora local del servidor.
- Soporta múltiples mundos.
- Configuración personalizable (zona horaria, intervalo de actualización).
- Comando para verificar la hora actual.

---

## Instalación
1. Descarga el archivo `TimeSync-1.0-SNAPSHOT.jar` desde la sección [Releases](https://github.com/EnriqueCazun/TimeSync/releases/tag/1.0).
2. Coloca el archivo en la carpeta `plugins` de tu servidor de Minecraft.
3. Reinicia el servidor.

---

## Uso
### Comandos
- `/timesync time`  
  Muestra la hora actual del servidor en la zona horaria configurada.
  
---

## Contribuciones
Los reportes de errores y sugerencias son bienvenidos. Abre un issue para discutir cambios.

---

## Configuración
Edita el archivo `plugins/TimeSync/config.yml`:

```yaml
# Zona horaria (ver lista: https://tinyurl.com/tz-db)
timezone: "America/Guayaquil"

# Mundos a sincronizar
worlds:
  - "world"
  - "world_nether"

# Intervalo de actualización (en segundos)
update-interval: 10
