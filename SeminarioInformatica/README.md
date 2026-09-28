# SIGMA — Sistema de Gestión y Monitoreo de Accesos (VigilPlus S.A.)

Prototipo operacional desarrollado para INF390 — Seminario de Práctica.

| Versión | Entrega | Alcance |
|---|---|---|
| 1.0.0 | TP1 | Caso de uso *Autorizar acceso* |
| 2.0.0 | TP2 | Modelo de datos normalizado (3FN), gestión de credenciales, visitantes con QR temporal, alertas por sensor de puerta, historial, estado de puntos, vista de consola con login y pruebas JUnit |

## Estructura

```
├── pom.xml
├── docs/diagramas/                 # Fuentes PlantUML/Graphviz y PNG de los diagramas UML, DER y red
├── sql/
│   ├── 01_esquema.sql               # CREATE DATABASE / TABLE / VIEW / USER
│   ├── 02_datos.sql                 # Datos de ejemplo (INSERT)
│   ├── 03_consultas.sql             # Consultas (SELECT) C1..C7
│   └── 04_actualizacion_borrado.sql # UPDATE y DELETE
└── src/
    ├── main/java/com/vigilplus/sigma/
    │   ├── Main.java
    │   ├── vista/          VistaConsola
    │   ├── controlador/    ControladorAcceso, ControladorVisitas, ControladorCredenciales,
    │   │                   ControladorAlertas, ControladorConsultas
    │   ├── modelo/         Credencial, Persona, EventoAcceso, MotivoRechazo, Alerta, ...
    │   ├── persistencia/   Conexion, Transaccion y DAOs (JDBC)
    │   └── util/           Seguridad (SHA-256, códigos QR)
    └── test/java/com/vigilplus/sigma/
        ├── modelo/CredencialTest.java          # pruebas unitarias
        ├── util/SeguridadTest.java             # pruebas unitarias
        └── integracion/SigmaIntegracionIT.java # pruebas de integración (MySQL)
```

## Cómo ejecutar

1. Crear la base (MySQL 8.0.16 o superior):
   ```
   mysql -u root -p < sql/01_esquema.sql
   mysql -u root -p < sql/02_datos.sql
   ```
2. Configurar la conexión (opcional; por defecto `localhost:3306`, usuario `sigma_app`):
   variables de entorno `SIGMA_DB_URL`, `SIGMA_DB_USER`, `SIGMA_DB_PASS`.
3. Ejecutar:
   ```
   mvn compile exec:java                               # vista de consola (login)
   mvn compile exec:java -Dexec.args="RFID-0001 1"     # simula una lectura en el punto 1
   ```
   Usuarios de prueba: `mfernandez / Operador#2026` (operador), `lgimenez / Admin#2026` (administrador).
4. Pruebas:
   ```
   mvn test      # unitarias
   mvn verify    # unitarias + integración (requiere la base creada)
   ```
