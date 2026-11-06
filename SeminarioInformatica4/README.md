# SIGMA — Sistema de Gestión y Monitoreo de Accesos (VigilPlus S.A.)

Prototipo operacional desarrollado para INF390 — Seminario de Práctica.
Versión **4.0.0** (TP4, proyecto integrador). Autor: Ariel Sebastián Angeleri.

## Arquitectura y patrón de diseño

Arquitectura en capas **MVC** (vista → controlador → modelo/persistencia) con el patrón
**DAO** en la capa de persistencia:

- `persistencia.Dao<T, K>`: interfaz con el contrato de cada DAO (`buscarPorId`, `listarTodos`).
- `persistencia.DaoBase<T>`: clase abstracta (Template Method) con todo el código JDBC común;
  cada DAO concreto solo implementa `mapear(ResultSet)`.
- `persistencia.Transaccion`: commit/rollback y traducción de `SQLException` a `PersistenciaException`.

## Paquetes

| Paquete | Contenido |
|---|---|
| `vista` | `VistaConsola`: menú, inicio de sesión y presentación de resultados |
| `controlador` | Acceso, Visitas, Credenciales, Alertas, Consultas y Reportes |
| `modelo` | Credencial, Persona, EventoAcceso, PadronLocal, EntradaPadron, interfaz `Exportable`, records |
| `modelo.regla` | Clase abstracta `ReglaAcceso` y sus subclases (estado, vigencia, punto habilitado) |
| `reporte` | `ResumenAlertas` (arreglo `int[]` por severidad + ranking por complejo) |
| `persistencia` | `Dao`, `DaoBase`, `Conexion`, `Transaccion` y los DAO de cada entidad |
| `excepcion` | `SigmaException` y sus subclases (Persistencia, DatoInvalido, PermisoDenegado, Archivo) |
| `util` | `Seguridad`, `Algoritmos` (inserción, búsqueda secuencial), `ArchivoCsv` |

## Cómo ejecutar

Requisitos: Java 17+, Maven, MySQL 8.0.16+.

```
mysql -u root -p < sql/01_esquema.sql
mysql -u root -p < sql/02_datos.sql
mvn compile exec:java                                   # consola interactiva
mvn compile exec:java -Dexec.args="RFID-0001 1"         # simula una lectura de credencial
mvn verify                                              # 29 pruebas unitarias + 21 de integración
```

Usuarios de ejemplo: `mfernandez` / `Operador#2026` (operador) y `lgimenez` / `Admin#2026` (administrador).
Los datos de conexión se configuran con `-Dsigma.db.url`, `-Dsigma.db.user`, `-Dsigma.db.pass`
o las variables `SIGMA_DB_URL`, `SIGMA_DB_USER`, `SIGMA_DB_PASS`.

## Archivos

- Opción 10: exporta el historial de accesos a CSV (separador `;`, abre en Excel).
- Opción 11: exporta el padrón de credenciales de un complejo y lo vuelve a cargar para validar
  accesos sin conexión (búsqueda binaria sobre un arreglo ordenado).

## Documentación

Diagramas UML (fuentes PlantUML y PNG) en `docs/diagramas/`. Los f01 a f14 corresponden al TP2;
f15 a f19, al TP4 (patrón DAO, reglas y modelo, excepciones, secuencia de error y paquetes).
