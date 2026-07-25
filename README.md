# ReportesBIRT — Servicio de reportería del Sistema de Administración de Flotas

Proyecto Java independiente que genera los reportes oficiales del caso de estudio en PDF con
**Eclipse BIRT**, sin necesidad de instalar el IDE de Eclipse. El backend de dominio
(`ProyectoFlota`) lo invoca como proceso externo y devuelve el PDF al frontend.

Reporte implementado: **Control Mensual de Costo Operacional de Vehículo (MA 122 03 01)**.

## Requisitos

- **JDK 21** (por ejemplo Eclipse Temurin).
- **Maven 3.9+**. Si no está instalado como comando global, se puede descomprimir el zip de
  [maven.apache.org](https://maven.apache.org/download.cgi) y agregar su carpeta `bin` al `PATH`
  de la sesión.
- **PostgreSQL** con la base `flotasys` ya migrada por el backend.

## Compilar

```bash
mvn package
```

Esto descarga el motor de BIRT (`birt-runtime-bundle`) y sus dependencias como jars sueltos en
`target/lib/`. **No se genera un jar sombreado a propósito**: BIRT descubre sus plugins escaneando
el `plugin.xml` de cada jar del classpath, y fusionarlos en un uber-jar rompe ese descubrimiento.

## Conexión a la base de datos

Los `.rptdesign` **no llevan la contraseña versionada**. El origen de datos `FlotasysDB` define
solo valores por defecto y la conexión real se inyecta en tiempo de ejecución por variables de
entorno:

```env
BIRT_DB_URL=jdbc:postgresql://localhost:5432/flotasys
BIRT_DB_USER=postgres
BIRT_DB_PASSWORD=tu_password
```

Cuando el reporte se genera **a través del backend**, este las arma automáticamente a partir de su
propia `DATABASE_URL` y no hay que configurar nada. Solo hay que exportarlas para ejecutar
`GenerarReporte` de forma independiente.

Las consultas de los reportes **cualifican el esquema de cada tabla** (por ejemplo
`"costos"."CostoOperacionMensual"` y `"operacion"."Vehiculo"`), porque el modelo está separado en
siete esquemas de dominio. Si se agrega un reporte nuevo, sus consultas deben hacer lo mismo: sin
el esquema, BIRT no encuentra la tabla y emite el PDF con los encabezados pero **sin filas**.

## Generar un reporte

```bash
export BIRT_DB_URL=jdbc:postgresql://localhost:5432/flotasys
export BIRT_DB_USER=postgres
export BIRT_DB_PASSWORD=tu_password

java -cp "target/classes;target/lib/*" com.flotasys.reportes.GenerarReporte \
  reports/control-costo-operacional.rptdesign \
  output/control-costo-operacional.pdf \
  pMesAnio=2024-05
```

En Linux/macOS el separador del classpath es `:` en vez de `;`; en PowerShell se usa
`$env:BIRT_DB_URL="..."` en lugar de `export`.

El mes (`YYYY-MM`) debe existir en la tabla `costos.CostoOperacionMensual`, que se llena desde el
backend con `POST /api/costos/calcular`.

## Nombres internos frente a la API HTTP

Conviene no confundir ambos planos, porque no coinciden:

| Plano | Nombre |
| --- | --- |
| Plantilla BIRT | `control-costo-operacional.rptdesign` |
| Parámetro del reporte | `pMesAnio` |
| Ruta de la API | `GET /api/reportes/costo-operacional` |
| Parámetro de la API | `mesAnio` |

El backend recibe `mesAnio`, elige la plantilla y traduce el parámetro a `pMesAnio`. El cliente
web solo debe usar la ruta y el parámetro de la API.

## Estructura

- `src/main/java/com/flotasys/reportes/GenerarReporte.java` — punto de entrada; ejecuta la
  plantilla y escribe el PDF.
- `src/main/java/com/flotasys/reportes/TestConexion.java` — comprobación suelta de la conexión JDBC.
- `reports/` — plantillas `.rptdesign`.
- `output/` — PDFs generados (no se versionan).

## Relación con los demás proyectos

La guía completa para levantar el sistema entero (base de datos, backend, gateway, frontend y este
servicio) está en el README del repositorio `ProyectoFlota`.
