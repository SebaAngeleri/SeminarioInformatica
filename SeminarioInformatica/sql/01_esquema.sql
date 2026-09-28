-- =========================================================
-- SIGMA - Sistema de Gestion y Monitoreo de Accesos
-- VigilPlus S.A. - Script 01: creacion del esquema (MySQL 8)
-- Version 2.0 (TP2) - modelo relacional normalizado a 3FN
-- =========================================================

DROP DATABASE IF EXISTS sigma;
CREATE DATABASE sigma CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE sigma;

-- Locaciones (edificios corporativos o complejos residenciales)
CREATE TABLE complejo (
    id_complejo      INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL UNIQUE,
    direccion        VARCHAR(150) NOT NULL,
    tipo             ENUM('CORPORATIVO','RESIDENCIAL') NOT NULL,
    activo           BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- Puertas / molinetes equipados con lector RFID o QR
CREATE TABLE punto_acceso (
    id_punto_acceso  INT AUTO_INCREMENT PRIMARY KEY,
    id_complejo      INT NOT NULL,
    identificador    VARCHAR(50) NOT NULL,
    tipo_dispositivo ENUM('RFID','QR') NOT NULL,
    estado           ENUM('ACTIVO','INACTIVO','FALLA') NOT NULL DEFAULT 'ACTIVO',
    tiempo_max_apertura_seg SMALLINT UNSIGNED NOT NULL DEFAULT 30,
    ultima_senal     DATETIME NULL,
    CONSTRAINT uq_punto_complejo UNIQUE (id_complejo, identificador),
    CONSTRAINT chk_tiempo_apertura CHECK (tiempo_max_apertura_seg BETWEEN 5 AND 600),
    CONSTRAINT fk_punto_complejo FOREIGN KEY (id_complejo)
        REFERENCES complejo(id_complejo) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Sensores instalados en cada punto de acceso
CREATE TABLE sensor (
    id_sensor        INT AUTO_INCREMENT PRIMARY KEY,
    id_punto_acceso  INT NOT NULL,
    tipo             ENUM('APERTURA_PUERTA','PRESENCIA') NOT NULL,
    ubicacion        VARCHAR(100),
    CONSTRAINT fk_sensor_punto FOREIGN KEY (id_punto_acceso)
        REFERENCES punto_acceso(id_punto_acceso) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Catalogo de roles de las personas (antes ENUM en persona)
CREATE TABLE rol (
    id_rol           TINYINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(30) NOT NULL UNIQUE,
    descripcion      VARCHAR(120)
) ENGINE=InnoDB;

-- Titulares de credenciales
CREATE TABLE persona (
    id_persona       INT AUTO_INCREMENT PRIMARY KEY,
    dni              VARCHAR(12) NOT NULL UNIQUE,
    nombre           VARCHAR(60) NOT NULL,
    apellido         VARCHAR(60) NOT NULL,
    email            VARCHAR(100),
    telefono         VARCHAR(20),
    id_rol           TINYINT NOT NULL,
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_persona_rol FOREIGN KEY (id_rol)
        REFERENCES rol(id_rol) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Usuarios que operan el sistema (operadores y administradores)
CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    id_persona       INT NOT NULL UNIQUE,
    username         VARCHAR(30) NOT NULL UNIQUE,
    password_hash    CHAR(64) NOT NULL,          -- SHA-256 en hexadecimal
    perfil           ENUM('OPERADOR','ADMINISTRADOR') NOT NULL,
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuario_persona FOREIGN KEY (id_persona)
        REFERENCES persona(id_persona) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Credenciales RFID / QR (permanentes o temporales)
CREATE TABLE credencial (
    id_credencial    INT AUTO_INCREMENT PRIMARY KEY,
    id_persona       INT NOT NULL,
    codigo           VARCHAR(50) NOT NULL UNIQUE,
    tipo             ENUM('RFID','QR') NOT NULL,
    vigencia_desde   DATETIME NOT NULL,
    vigencia_hasta   DATETIME NOT NULL,
    estado           ENUM('ACTIVA','SUSPENDIDA','BAJA') NOT NULL DEFAULT 'ACTIVA',
    temporal         BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_vigencia CHECK (vigencia_hasta > vigencia_desde),
    CONSTRAINT fk_credencial_persona FOREIGN KEY (id_persona)
        REFERENCES persona(id_persona) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Relacion N:M "Habilita": puntos de acceso permitidos por credencial
CREATE TABLE credencial_punto_acceso (
    id_credencial    INT NOT NULL,
    id_punto_acceso  INT NOT NULL,
    PRIMARY KEY (id_credencial, id_punto_acceso),
    CONSTRAINT fk_cpa_credencial FOREIGN KEY (id_credencial)
        REFERENCES credencial(id_credencial) ON DELETE CASCADE,
    CONSTRAINT fk_cpa_punto FOREIGN KEY (id_punto_acceso)
        REFERENCES punto_acceso(id_punto_acceso) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Visitas: acceso temporal registrado por un operador (RF07)
CREATE TABLE visita (
    id_visita        INT AUTO_INCREMENT PRIMARY KEY,
    id_persona       INT NOT NULL,
    id_credencial    INT NOT NULL UNIQUE,
    id_complejo      INT NOT NULL,
    id_usuario_registra INT NOT NULL,
    anfitrion        VARCHAR(100) NOT NULL,
    motivo           VARCHAR(150),
    fecha_registro   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visita_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    CONSTRAINT fk_visita_credencial FOREIGN KEY (id_credencial)
        REFERENCES credencial(id_credencial) ON DELETE CASCADE,
    CONSTRAINT fk_visita_complejo FOREIGN KEY (id_complejo) REFERENCES complejo(id_complejo),
    CONSTRAINT fk_visita_usuario FOREIGN KEY (id_usuario_registra) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- Eventos de ingreso/egreso (autorizados o rechazados)
-- id_credencial es NULL cuando el codigo leido no existe en el sistema;
-- en ese caso se conserva el codigo en codigo_no_reconocido (exactamente uno de los dos).
CREATE TABLE evento_acceso (
    id_evento        BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_punto_acceso  INT NOT NULL,
    id_credencial    INT NULL,
    codigo_no_reconocido VARCHAR(50) NULL,
    fecha_hora       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    sentido          ENUM('INGRESO','EGRESO') NOT NULL,
    resultado        ENUM('AUTORIZADO','RECHAZADO') NOT NULL,
    motivo_rechazo   VARCHAR(120) NULL,
    CONSTRAINT chk_evento_origen CHECK ((id_credencial IS NULL) <> (codigo_no_reconocido IS NULL)),
    CONSTRAINT fk_evento_punto FOREIGN KEY (id_punto_acceso) REFERENCES punto_acceso(id_punto_acceso),
    CONSTRAINT fk_evento_credencial FOREIGN KEY (id_credencial) REFERENCES credencial(id_credencial),
    INDEX idx_evento_fecha (fecha_hora),
    INDEX idx_evento_credencial_fecha (id_credencial, fecha_hora),
    INDEX idx_evento_punto_fecha (id_punto_acceso, fecha_hora)
) ENGINE=InnoDB;

-- Lecturas de los sensores de apertura de puerta (RF05)
CREATE TABLE evento_sensor (
    id_evento_sensor BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sensor        INT NOT NULL,
    fecha_hora       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    estado_puerta    ENUM('ABIERTA','CERRADA') NOT NULL,
    CONSTRAINT fk_evsensor_sensor FOREIGN KEY (id_sensor) REFERENCES sensor(id_sensor),
    INDEX idx_evsensor_sensor_fecha (id_sensor, fecha_hora)
) ENGINE=InnoDB;

-- Catalogo de tipos de alerta (antes texto libre en alerta.tipo)
CREATE TABLE tipo_alerta (
    id_tipo_alerta   TINYINT AUTO_INCREMENT PRIMARY KEY,
    codigo           VARCHAR(30) NOT NULL UNIQUE,
    descripcion      VARCHAR(120) NOT NULL,
    severidad        ENUM('BAJA','MEDIA','ALTA') NOT NULL
) ENGINE=InnoDB;

-- Alertas: se originan en un evento de acceso o en una lectura de sensor
-- (exactamente uno). El punto de acceso se obtiene a traves del origen.
CREATE TABLE alerta (
    id_alerta        BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_alerta   TINYINT NOT NULL,
    id_evento        BIGINT NULL,
    id_evento_sensor BIGINT NULL,
    fecha_hora       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    estado           ENUM('PENDIENTE','RESUELTA') NOT NULL DEFAULT 'PENDIENTE',
    id_usuario_resuelve INT NULL,
    fecha_resolucion DATETIME NULL,
    observaciones    VARCHAR(255) NULL,
    CONSTRAINT chk_alerta_origen CHECK ((id_evento IS NULL) <> (id_evento_sensor IS NULL)),
    CONSTRAINT fk_alerta_tipo FOREIGN KEY (id_tipo_alerta) REFERENCES tipo_alerta(id_tipo_alerta),
    CONSTRAINT fk_alerta_evento FOREIGN KEY (id_evento) REFERENCES evento_acceso(id_evento),
    CONSTRAINT fk_alerta_evsensor FOREIGN KEY (id_evento_sensor) REFERENCES evento_sensor(id_evento_sensor),
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (id_usuario_resuelve) REFERENCES usuario(id_usuario),
    CONSTRAINT uq_alerta_evsensor_tipo UNIQUE (id_evento_sensor, id_tipo_alerta),
    INDEX idx_alerta_estado_fecha (estado, fecha_hora)
) ENGINE=InnoDB;

-- Vista de apoyo: alertas con su punto de acceso y complejo resueltos
CREATE VIEW v_alerta_detalle AS
SELECT a.id_alerta, a.fecha_hora, ta.codigo AS tipo, ta.severidad, a.estado,
       pa.id_punto_acceso, pa.identificador AS punto_acceso, c.nombre AS complejo,
       a.id_evento, a.id_evento_sensor, a.observaciones
FROM alerta a
JOIN tipo_alerta ta        ON ta.id_tipo_alerta = a.id_tipo_alerta
LEFT JOIN evento_acceso ea ON ea.id_evento = a.id_evento
LEFT JOIN evento_sensor es ON es.id_evento_sensor = a.id_evento_sensor
LEFT JOIN sensor s         ON s.id_sensor = es.id_sensor
JOIN punto_acceso pa       ON pa.id_punto_acceso = COALESCE(ea.id_punto_acceso, s.id_punto_acceso)
JOIN complejo c            ON c.id_complejo = pa.id_complejo;

-- Usuario de aplicacion con privilegios minimos (principio de menor privilegio)
CREATE USER IF NOT EXISTS 'sigma_app'@'localhost' IDENTIFIED BY 'cambiar_en_produccion';
GRANT SELECT, INSERT, UPDATE, DELETE ON sigma.* TO 'sigma_app'@'localhost';
FLUSH PRIVILEGES;
