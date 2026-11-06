-- =========================================================
-- SIGMA - Script 04: actualizacion y borrado de registros
-- =========================================================
USE sigma;

-- U1 (RF01) Baja logica de la credencial vencida del proveedor.
--    Se prefiere la baja logica al DELETE para no perder la trazabilidad
--    de los eventos historicos que la referencian.
UPDATE credencial SET estado = 'BAJA' WHERE codigo = 'QR-PROV-0004';

-- U2 Resolucion de una alerta pendiente por parte del operador
UPDATE alerta
SET estado = 'RESUELTA', id_usuario_resuelve = 1, fecha_resolucion = NOW(),
    observaciones = 'Intento con tarjeta clonada; se informo al cliente'
WHERE id_alerta = 2 AND estado = 'PENDIENTE';

-- D1 Borrado fisico: se revoca el permiso de RFID-0001 sobre el Hall Central
DELETE FROM credencial_punto_acceso
WHERE id_credencial = (SELECT id_credencial FROM credencial WHERE codigo = 'RFID-0001')
  AND id_punto_acceso = 4;

-- D2 Depuracion: lecturas de sensor de mas de 1 anio que no originaron alertas
DELETE es FROM evento_sensor es
LEFT JOIN alerta a ON a.id_evento_sensor = es.id_evento_sensor
WHERE a.id_alerta IS NULL
  AND es.fecha_hora < NOW() - INTERVAL 1 YEAR;

-- D3 Intento de borrar una credencial con eventos asociados:
--    la FK fk_evento_credencial (ON DELETE RESTRICT) lo impide y preserva la integridad.
-- DELETE FROM credencial WHERE codigo = 'RFID-0002';
--   -> ERROR 1451: Cannot delete or update a parent row: a foreign key constraint fails

-- Verificacion de los cambios
SELECT codigo, estado FROM credencial WHERE codigo = 'QR-PROV-0004';
SELECT id_alerta, estado, fecha_resolucion IS NOT NULL AS con_fecha_resolucion FROM alerta WHERE id_alerta = 2;
SELECT COUNT(*) AS permisos_rfid_0001 FROM credencial_punto_acceso WHERE id_credencial = 1;
