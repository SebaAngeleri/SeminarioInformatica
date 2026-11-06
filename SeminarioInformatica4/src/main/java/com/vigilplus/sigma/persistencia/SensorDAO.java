package com.vigilplus.sigma.persistencia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/** DAO de sensores y de sus lecturas (tabla evento_sensor). */
public class SensorDAO extends DaoBase<SensorDAO.AperturaExcedida> {

    /** Apertura de puerta que supero el tiempo maximo configurado en su punto de acceso. */
    public record AperturaExcedida(long idEventoSensor, int idPuntoAcceso, LocalDateTime desde) { }

    @Override
    protected AperturaExcedida mapear(ResultSet rs) throws SQLException {
        return new AperturaExcedida(rs.getLong("id_evento_sensor"), rs.getInt("id_punto_acceso"),
                fecha(rs, "fecha_hora"));
    }

    /** Devuelve el punto de acceso del sensor, o null si el sensor no existe. */
    public Integer puntoDelSensor(Connection con, int idSensor) throws SQLException {
        List<Integer> puntos = consultar(con, "SELECT id_punto_acceso FROM sensor WHERE id_sensor = ?",
                rs -> rs.getInt(1), idSensor);
        return puntos.isEmpty() ? null : puntos.get(0);
    }

    public long registrarLectura(Connection con, int idSensor, String estadoPuerta, LocalDateTime fecha)
            throws SQLException {
        return insertar(con, "INSERT INTO evento_sensor (id_sensor, fecha_hora, estado_puerta) VALUES (?, ?, ?)",
                idSensor, fecha, estadoPuerta);
    }

    /**
     * RF05: sensores cuya ultima lectura es ABIERTA y cuya antiguedad supera
     * el tiempo maximo de apertura del punto de acceso.
     */
    public List<AperturaExcedida> aperturasExcedidas(Connection con, LocalDateTime ahora) throws SQLException {
        return consultarLista(con, "SELECT es.id_evento_sensor, s.id_punto_acceso, es.fecha_hora "
                + "FROM evento_sensor es "
                + "JOIN sensor s ON s.id_sensor = es.id_sensor "
                + "JOIN punto_acceso pa ON pa.id_punto_acceso = s.id_punto_acceso "
                + "WHERE es.estado_puerta = 'ABIERTA' "
                + "  AND es.fecha_hora = (SELECT MAX(x.fecha_hora) FROM evento_sensor x WHERE x.id_sensor = es.id_sensor) "
                + "  AND TIMESTAMPDIFF(SECOND, es.fecha_hora, ?) > pa.tiempo_max_apertura_seg", ahora);
    }
}
