package com.vigilplus.sigma.persistencia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de sensores y de sus lecturas (tabla evento_sensor). */
public class SensorDAO {

    /** Apertura de puerta que supero el tiempo maximo configurado en su punto de acceso. */
    public record AperturaExcedida(long idEventoSensor, int idPuntoAcceso, LocalDateTime desde) { }

    public int puntoDelSensor(Connection con, int idSensor) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_punto_acceso FROM sensor WHERE id_sensor = ?")) {
            ps.setInt(1, idSensor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Sensor inexistente: " + idSensor);
                return rs.getInt(1);
            }
        }
    }

    public long registrarLectura(Connection con, int idSensor, String estadoPuerta, LocalDateTime fecha)
            throws SQLException {
        String sql = "INSERT INTO evento_sensor (id_sensor, fecha_hora, estado_puerta) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idSensor);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setString(3, estadoPuerta);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getLong(1);
            }
        }
    }

    /**
     * RF05: sensores cuya ultima lectura es ABIERTA y cuya antiguedad supera
     * el tiempo maximo de apertura del punto de acceso.
     */
    public List<AperturaExcedida> aperturasExcedidas(Connection con, LocalDateTime ahora) throws SQLException {
        String sql = "SELECT es.id_evento_sensor, s.id_punto_acceso, es.fecha_hora "
                   + "FROM evento_sensor es "
                   + "JOIN sensor s ON s.id_sensor = es.id_sensor "
                   + "JOIN punto_acceso pa ON pa.id_punto_acceso = s.id_punto_acceso "
                   + "WHERE es.estado_puerta = 'ABIERTA' "
                   + "  AND es.fecha_hora = (SELECT MAX(x.fecha_hora) FROM evento_sensor x WHERE x.id_sensor = es.id_sensor) "
                   + "  AND TIMESTAMPDIFF(SECOND, es.fecha_hora, ?) > pa.tiempo_max_apertura_seg";
        List<AperturaExcedida> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(ahora));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AperturaExcedida(rs.getLong(1), rs.getInt(2),
                            rs.getTimestamp(3).toLocalDateTime()));
                }
            }
        }
        return lista;
    }
}
