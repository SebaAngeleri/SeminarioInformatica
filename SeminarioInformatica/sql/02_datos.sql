-- =========================================================
-- SIGMA - Script 02: insercion de datos de ejemplo
-- =========================================================
USE sigma;

INSERT INTO complejo (nombre, direccion, tipo) VALUES
  ('Torre Libertador',    'Av. del Libertador 4500, CABA',     'CORPORATIVO'),
  ('Complejo Los Aromos', 'Ruta 8 km 45, San Martin, BA',      'RESIDENCIAL'),
  ('Edificio Catalinas',  'Av. Madero 1020, CABA',             'CORPORATIVO');

INSERT INTO punto_acceso (id_complejo, identificador, tipo_dispositivo, estado, tiempo_max_apertura_seg, ultima_senal) VALUES
  (1, 'Puerta Principal',         'RFID', 'ACTIVO', 30, NOW()),
  (1, 'Molinete Estacionamiento', 'RFID', 'ACTIVO', 20, NOW()),
  (2, 'Porton Peatonal',          'QR',   'ACTIVO', 45, NOW()),
  (3, 'Hall Central',             'RFID', 'FALLA',  30, NOW() - INTERVAL 2 HOUR);

INSERT INTO sensor (id_punto_acceso, tipo, ubicacion) VALUES
  (1, 'APERTURA_PUERTA', 'Marco superior'),
  (2, 'APERTURA_PUERTA', 'Brazo del molinete'),
  (3, 'APERTURA_PUERTA', 'Marco superior'),
  (4, 'APERTURA_PUERTA', 'Marco superior');

INSERT INTO rol (nombre, descripcion) VALUES
  ('OPERADOR',  'Personal de seguridad de VigilPlus'),
  ('EMPLEADO',  'Empleado o residente de un complejo cliente'),
  ('VISITANTE', 'Persona externa con acceso temporal'),
  ('PROVEEDOR', 'Proveedor recurrente de un complejo');

INSERT INTO persona (dni, nombre, apellido, email, telefono, id_rol) VALUES
  ('28111222', 'Marcos',  'Fernandez', 'mfernandez@vigilplus.com.ar', '11-4000-1001', 1),
  ('33444555', 'Julieta', 'Sosa',      'jsosa@torrelib.com.ar',       '11-4000-2002', 2),
  ('30999888', 'Diego',   'Paz',       'dpaz@aromos.com.ar',          '11-4000-3003', 2),
  ('27666777', 'Laura',   'Gimenez',   'lgimenez@vigilplus.com.ar',   '11-4000-4004', 1),
  ('40123456', 'Tomas',   'Ruiz',      NULL,                          '11-5000-5005', 3),
  ('35222333', 'Carla',   'Medina',    'cmedina@limpiezasur.com.ar',  '11-5000-6006', 4);

INSERT INTO usuario (id_persona, username, password_hash, perfil) VALUES
  (1, 'mfernandez', SHA2('Operador#2026', 256), 'OPERADOR'),
  (4, 'lgimenez',   SHA2('Admin#2026', 256),    'ADMINISTRADOR');

INSERT INTO credencial (id_persona, codigo, tipo, vigencia_desde, vigencia_hasta, estado, temporal) VALUES
  (1, 'RFID-0001',    'RFID', '2026-01-01 00:00', '2027-12-31 23:59', 'ACTIVA',     FALSE),
  (2, 'RFID-0002',    'RFID', '2026-01-01 00:00', '2027-12-31 23:59', 'ACTIVA',     FALSE),
  (3, 'QR-0003',      'QR',   '2026-01-01 00:00', '2027-12-31 23:59', 'ACTIVA',     FALSE),
  (6, 'QR-PROV-0004', 'QR',   '2026-03-01 00:00', '2026-09-10 23:59', 'ACTIVA',     FALSE),
  (2, 'RFID-0005',    'RFID', '2025-01-01 00:00', '2027-12-31 23:59', 'SUSPENDIDA', FALSE),
  (5, 'QR-V-7K2M9P4X', 'QR',  '2026-09-25 14:00', '2026-09-25 18:00', 'ACTIVA',     TRUE);

INSERT INTO credencial_punto_acceso (id_credencial, id_punto_acceso) VALUES
  (1, 1), (1, 2), (1, 3), (1, 4),
  (2, 1), (2, 2),
  (3, 3),
  (4, 3),
  (5, 1),
  (6, 3);

INSERT INTO evento_acceso (id_punto_acceso, id_credencial, codigo_no_reconocido, fecha_hora, sentido, resultado, motivo_rechazo) VALUES
  (1, 2,    NULL,        '2026-09-21 08:02:11', 'INGRESO', 'AUTORIZADO', NULL),
  (2, 2,    NULL,        '2026-09-21 08:05:40', 'INGRESO', 'AUTORIZADO', NULL),
  (1, 2,    NULL,        '2026-09-21 18:15:03', 'EGRESO',  'AUTORIZADO', NULL),
  (3, 3,    NULL,        '2026-09-22 07:30:55', 'EGRESO',  'AUTORIZADO', NULL),
  (3, 4,    NULL,        '2026-09-22 10:12:09', 'INGRESO', 'RECHAZADO',  'Credencial vencida'),
  (1, NULL, 'RFID-9999', '2026-09-23 02:41:17', 'INGRESO', 'RECHAZADO',  'Credencial inexistente'),
  (3, 2,    NULL,        '2026-09-24 09:00:00', 'INGRESO', 'RECHAZADO',  'Credencial no habilitada para este punto de acceso'),
  (1, 1,    NULL,        '2026-09-25 06:58:30', 'INGRESO', 'AUTORIZADO', NULL),
  (3, 6,    NULL,        '2026-09-25 14:05:12', 'INGRESO', 'AUTORIZADO', NULL);

-- Visita registrada por el operador mfernandez (RF07)
INSERT INTO visita (id_persona, id_credencial, id_complejo, id_usuario_registra, anfitrion, motivo, fecha_registro) VALUES
  (5, 6, 2, 1, 'Diego Paz', 'Entrega de documentacion', '2026-09-25 13:58:40');

INSERT INTO evento_sensor (id_sensor, fecha_hora, estado_puerta) VALUES
  (1, '2026-09-21 08:02:12', 'ABIERTA'),
  (1, '2026-09-21 08:02:20', 'CERRADA'),
  (3, '2026-09-24 21:10:00', 'ABIERTA'),
  (3, '2026-09-24 21:12:30', 'CERRADA');

INSERT INTO tipo_alerta (codigo, descripcion, severidad) VALUES
  ('CRED_VENCIDA',        'Uso de credencial vencida',                         'MEDIA'),
  ('CRED_NO_AUTORIZADA',  'Credencial no habilitada, suspendida o dada de baja', 'MEDIA'),
  ('CRED_INEXISTENTE',    'Codigo leido no registrado en el sistema',          'ALTA'),
  ('PUERTA_ABIERTA',      'Puerta abierta mas alla del tiempo configurado',    'MEDIA'),
  ('PUERTA_FORZADA',      'Apertura de puerta sin acceso autorizado previo',   'ALTA');

INSERT INTO alerta (id_tipo_alerta, id_evento, id_evento_sensor, fecha_hora, estado, id_usuario_resuelve, fecha_resolucion, observaciones) VALUES
  (1, 5,    NULL, '2026-09-22 10:12:09', 'RESUELTA',  1, '2026-09-22 10:20:00', 'Se contacto al proveedor para renovar la credencial'),
  (3, 6,    NULL, '2026-09-23 02:41:17', 'PENDIENTE', NULL, NULL, NULL),
  (2, 7,    NULL, '2026-09-24 09:00:00', 'PENDIENTE', NULL, NULL, NULL),
  (5, NULL, 3,    '2026-09-24 21:10:00', 'PENDIENTE', NULL, NULL, NULL),
  (4, NULL, 3,    '2026-09-24 21:10:45', 'RESUELTA',  1, '2026-09-24 21:13:00', 'Puerta cerrada por el operador de turno');
