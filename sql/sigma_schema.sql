-- =========================================================
-- SIGMA — Sistema de Gestión y Monitoreo de Accesos
-- VigilPlus S.A.
-- Script de creación de base de datos (MySQL)
-- =========================================================

DROP DATABASE IF EXISTS sigma;
CREATE DATABASE sigma CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE sigma;

-- ---------------------------------------------------------
-- Tabla: complejo
-- Representa cada locación (edificio corporativo o
-- complejo residencial) atendida por VigilPlus S.A.
-- ---------------------------------------------------------
CREATE TABLE complejo (
    id_complejo     INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    direccion       VARCHAR(150) NOT NULL,
    tipo            ENUM('CORPORATIVO', 'RESIDENCIAL') NOT NULL
);

-- ---------------------------------------------------------
-- Tabla: punto_acceso
-- Representa cada puerta/molinete equipado con lector
-- y sensor dentro de un complejo.
-- ---------------------------------------------------------
CREATE TABLE punto_acceso (
    id_punto_acceso INT AUTO_INCREMENT PRIMARY KEY,
    id_complejo     INT NOT NULL,
    identificador   VARCHAR(50) NOT NULL,
    tipo_dispositivo ENUM('RFID', 'QR') NOT NULL,
    estado          ENUM('ACTIVO', 'INACTIVO', 'FALLA') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_punto_complejo FOREIGN KEY (id_complejo)
        REFERENCES complejo(id_complejo)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Tabla: sensor
-- Sensores asociados a un punto de acceso (apertura de
-- puerta, condiciones ambientales, etc.).
-- ---------------------------------------------------------
CREATE TABLE sensor (
    id_sensor       INT AUTO_INCREMENT PRIMARY KEY,
    id_punto_acceso INT NOT NULL,
    tipo            VARCHAR(50) NOT NULL,
    ubicacion       VARCHAR(100),
    CONSTRAINT fk_sensor_punto FOREIGN KEY (id_punto_acceso)
        REFERENCES punto_acceso(id_punto_acceso)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Tabla: persona
-- Titulares de credenciales: personal de VigilPlus,
-- empleados de los clientes o visitantes.
-- ---------------------------------------------------------
CREATE TABLE persona (
    id_persona      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    rol             ENUM('OPERADOR', 'EMPLEADO', 'VISITANTE') NOT NULL
);

-- ---------------------------------------------------------
-- Tabla: credencial
-- Credencial (RFID/QR) asociada a una persona, habilitada
-- para uno o más puntos de acceso.
-- ---------------------------------------------------------
CREATE TABLE credencial (
    id_credencial   INT AUTO_INCREMENT PRIMARY KEY,
    id_persona      INT NOT NULL,
    codigo          VARCHAR(50) NOT NULL UNIQUE,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE NOT NULL,
    CONSTRAINT fk_credencial_persona FOREIGN KEY (id_persona)
        REFERENCES persona(id_persona)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Tabla: credencial_punto_acceso
-- Relación N:M entre credenciales y puntos de acceso
-- habilitados para esa credencial.
-- ---------------------------------------------------------
CREATE TABLE credencial_punto_acceso (
    id_credencial   INT NOT NULL,
    id_punto_acceso INT NOT NULL,
    PRIMARY KEY (id_credencial, id_punto_acceso),
    CONSTRAINT fk_cpa_credencial FOREIGN KEY (id_credencial)
        REFERENCES credencial(id_credencial) ON DELETE CASCADE,
    CONSTRAINT fk_cpa_punto FOREIGN KEY (id_punto_acceso)
        REFERENCES punto_acceso(id_punto_acceso) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Tabla: evento_acceso
-- Registro de cada intento de acceso (autorizado o
-- rechazado) detectado en un punto de acceso.
-- ---------------------------------------------------------
CREATE TABLE evento_acceso (
    id_evento       BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_credencial   INT NOT NULL,
    id_punto_acceso INT NOT NULL,
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sentido         ENUM('INGRESO', 'EGRESO') NOT NULL,
    resultado       ENUM('AUTORIZADO', 'RECHAZADO') NOT NULL,
    CONSTRAINT fk_evento_credencial FOREIGN KEY (id_credencial)
        REFERENCES credencial(id_credencial),
    CONSTRAINT fk_evento_punto FOREIGN KEY (id_punto_acceso)
        REFERENCES punto_acceso(id_punto_acceso)
);

-- ---------------------------------------------------------
-- Tabla: alerta
-- Alertas generadas ante condiciones anómalas
-- (credencial vencida, puerta forzada, etc.).
-- ---------------------------------------------------------
CREATE TABLE alerta (
    id_alerta       BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_punto_acceso INT NOT NULL,
    id_evento       BIGINT NULL,
    tipo            VARCHAR(60) NOT NULL,
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion     VARCHAR(255),
    CONSTRAINT fk_alerta_punto FOREIGN KEY (id_punto_acceso)
        REFERENCES punto_acceso(id_punto_acceso),
    CONSTRAINT fk_alerta_evento FOREIGN KEY (id_evento)
        REFERENCES evento_acceso(id_evento)
);

-- =========================================================
-- Datos de ejemplo para pruebas del prototipo
-- =========================================================
INSERT INTO complejo (nombre, direccion, tipo) VALUES
    ('Torre Libertador', 'Av. del Libertador 4500, CABA', 'CORPORATIVO'),
    ('Complejo Los Aromos', 'Ruta 8 km 45, San Martín, BA', 'RESIDENCIAL');

INSERT INTO punto_acceso (id_complejo, identificador, tipo_dispositivo, estado) VALUES
    (1, 'Puerta Principal', 'RFID', 'ACTIVO'),
    (1, 'Molinete Estacionamiento', 'RFID', 'ACTIVO'),
    (2, 'Portón Peatonal', 'QR', 'ACTIVO');

INSERT INTO sensor (id_punto_acceso, tipo, ubicacion) VALUES
    (1, 'Apertura de puerta', 'Marco superior'),
    (3, 'Apertura de puerta', 'Marco superior');

INSERT INTO persona (nombre_completo, rol) VALUES
    ('Marcos Fernández', 'OPERADOR'),
    ('Julieta Sosa', 'EMPLEADO'),
    ('Proveedor Externo SRL', 'VISITANTE');

INSERT INTO credencial (id_persona, codigo, fecha_vigencia_desde, fecha_vigencia_hasta) VALUES
    (1, 'RFID-0001', '2026-01-01', '2026-12-31'),
    (2, 'RFID-0002', '2026-01-01', '2026-12-31'),
    (3, 'QR-TEMP-0001', '2026-09-01', '2026-09-10');

INSERT INTO credencial_punto_acceso (id_credencial, id_punto_acceso) VALUES
    (1, 1), (1, 2),
    (2, 1),
    (3, 3);
