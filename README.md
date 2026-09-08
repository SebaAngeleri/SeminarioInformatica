# Prototipo SIGMA — VigilPlus S.A.

Prototipo operacional del caso de uso **Autorizar acceso**, correspondiente
al sistema de gestión y monitoreo de accesos desarrollado en el Trabajo
Práctico 1 de INF390 — Sistemas de Información

## Estructura

```
prototipo/
├── pom.xml
├── sql/
│   └── sigma_schema.sql        # Creación de la BD MySQL + datos de ejemplo
└── src/main/java/com/vigilplus/sigma/
    ├── Main.java                              # Punto de entrada de la demo
    ├── controlador/
    │   └── ControladorAcceso.java             # Lógica del caso de uso
    ├── modelo/
    │   ├── Credencial.java
    │   └── EventoAcceso.java
    └── persistencia/
        ├── Conexion.java                      # Conexión JDBC
        ├── CredencialDAO.java
        └── EventoAccesoDAO.java
```

## Cómo ejecutar

1. Crear la base de datos ejecutando `sql/sigma_schema.sql` en MySQL:
   ```
   mysql -u root -p < sql/sigma_schema.sql
   ```
2. Ajustar usuario y contraseña en `Conexion.java` si es necesario.
3. Compilar y ejecutar con Maven:
   ```
   mvn compile exec:java -Dexec.mainClass="com.vigilplus.sigma.Main" -Dexec.args="RFID-0001 1"
   ```
   El primer argumento es el código de la credencial y el segundo el
   identificador del punto de acceso (ver datos de ejemplo en el script SQL).

## Alcance del prototipo

Este prototipo cubre únicamente el caso de uso "Autorizar acceso" a modo
operacional, tal como se describe en la sección 7 del informe .
