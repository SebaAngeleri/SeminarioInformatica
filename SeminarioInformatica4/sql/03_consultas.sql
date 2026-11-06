-- =========================================================
-- SIGMA - Script 03: consultas
-- =========================================================
USE sigma;

-- C1 (RF06) Historial de accesos de una persona, en un complejo y rango de fechas
SELECT ea.fecha_hora, CONCAT(p.apellido, ', ', p.nombre) AS persona,
       pa.identificador AS punto, ea.sentido, ea.resultado
FROM evento_acceso ea
JOIN credencial cr  ON cr.id_credencial = ea.id_credencial
JOIN persona p      ON p.id_persona = cr.id_persona
JOIN punto_acceso pa ON pa.id_punto_acceso = ea.id_punto_acceso
JOIN complejo c     ON c.id_complejo = pa.id_complejo
WHERE p.dni = '33444555'
  AND c.nombre = 'Torre Libertador'
  AND ea.fecha_hora BETWEEN '2026-09-01' AND '2026-09-30 23:59:59'
ORDER BY ea.fecha_hora;

-- C2 (RF02) Verificacion de una credencial en un punto de acceso
SELECT cr.codigo, cr.estado,
       (NOW() BETWEEN cr.vigencia_desde AND cr.vigencia_hasta) AS vigente,
       EXISTS (SELECT 1 FROM credencial_punto_acceso cpa
               WHERE cpa.id_credencial = cr.id_credencial
                 AND cpa.id_punto_acceso = 3) AS habilitada_punto_3
FROM credencial cr
WHERE cr.codigo IN ('RFID-0001', 'QR-PROV-0004', 'RFID-0005');

-- C3 Alertas pendientes ordenadas por severidad (panel del operador)
SELECT id_alerta, fecha_hora, tipo, severidad, complejo, punto_acceso
FROM v_alerta_detalle
WHERE estado = 'PENDIENTE'
ORDER BY FIELD(severidad, 'ALTA', 'MEDIA', 'BAJA'), fecha_hora;

-- C4 (RF08) Estado en linea de los puntos de acceso (sin senal hace mas de 5 minutos = FUERA DE LINEA)
SELECT c.nombre AS complejo, pa.identificador AS punto, pa.estado,
       CASE WHEN pa.ultima_senal >= NOW() - INTERVAL 5 MINUTE
            THEN 'EN LINEA' ELSE 'FUERA DE LINEA' END AS conexion
FROM punto_acceso pa
JOIN complejo c ON c.id_complejo = pa.id_complejo
ORDER BY c.nombre, pa.identificador;

-- C5 Cantidad de accesos por complejo y resultado (reporte de gestion)
SELECT c.nombre AS complejo, ea.resultado, COUNT(*) AS cantidad
FROM evento_acceso ea
JOIN punto_acceso pa ON pa.id_punto_acceso = ea.id_punto_acceso
JOIN complejo c      ON c.id_complejo = pa.id_complejo
GROUP BY c.nombre, ea.resultado
ORDER BY c.nombre, ea.resultado;

-- C6 Credenciales ACTIVAS que ya vencieron (candidatas a baja) - subconsulta
SELECT cr.codigo, CONCAT(p.apellido, ', ', p.nombre) AS titular, r.nombre AS rol,
       cr.vigencia_hasta
FROM credencial cr
JOIN persona p ON p.id_persona = cr.id_persona
JOIN rol r     ON r.id_rol = p.id_rol
WHERE cr.estado = 'ACTIVA'
  AND cr.id_credencial IN (SELECT id_credencial FROM credencial WHERE vigencia_hasta < NOW());

-- C7 Personas con al menos un acceso rechazado (HAVING)
SELECT CONCAT(p.apellido, ', ', p.nombre) AS persona, COUNT(*) AS rechazos,
       MAX(ea.fecha_hora) AS ultimo_rechazo
FROM evento_acceso ea
JOIN credencial cr ON cr.id_credencial = ea.id_credencial
JOIN persona p     ON p.id_persona = cr.id_persona
WHERE ea.resultado = 'RECHAZADO'
GROUP BY p.id_persona, p.apellido, p.nombre
HAVING COUNT(*) >= 1
ORDER BY rechazos DESC;
