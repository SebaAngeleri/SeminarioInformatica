package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Alerta;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de la entidad Alerta (tablas alerta y tipo_alerta, vista v_alerta_detalle). */
public class AlertaDAO {

    /** Alerta originada en un evento de acceso rechazado. */
    public long generarPorEvento(Connection con, String codigoTipo, long idEvento, LocalDateTime fecha)
            throws SQLException {
        return insertar(con, codigoTipo, idEvento, null, fecha);
    }

    /** Alerta originada en una lectura de sensor de puerta. */
    public long generarPorSensor(Connection con, String codigoTipo, long idEventoSensor, LocalDateTime fecha)
            throws SQLException {
        return insertar(con, codigoTipo, null, idEventoSensor, fecha);
    }

    private long insertar(Connection con, String codigoTipo, Long idEvento, Long idEventoSensor,
                          LocalDateTime fecha) throws SQLException {
        String sql = "INSERT INTO alerta (id_tipo_alerta, id_evento, id_evento_sensor, fecha_hora) "
                   + "SELECT id_tipo_alerta, ?, ?, ? FROM tipo_alerta WHERE codigo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (idEvento == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, idEvento);
            if (idEventoSensor == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, idEventoSensor);
            ps.setTimestamp(3, Timestamp.valueOf(fecha));
            ps.setString(4, codigoTipo);
            if (ps.executeUpdate() == 0) throw new SQLException("Tipo de alerta inexistente: " + codigoTipo);
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getLong(1);
            }
        }
    }

    public boolean existeParaEventoSensor(Connection con, long idEventoSensor, String codigoTipo)
            throws SQLException {
        String sql = "SELECT 1 FROM alerta a JOIN tipo_alerta t ON t.id_tipo_alerta = a.id_tipo_alerta "
                   + "WHERE a.id_evento_sensor = ? AND t.codigo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idEventoSensor);
            ps.setString(2, codigoTipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Alerta> listarPendientes(Connection con) throws SQLException {
        String sql = "SELECT id_alerta, fecha_hora, tipo, severidad, estado, complejo, punto_acceso "
                   + "FROM v_alerta_detalle WHERE estado = 'PENDIENTE' "
                   + "ORDER BY FIELD(severidad, 'ALTA', 'MEDIA', 'BAJA'), fecha_hora";
        List<Alerta> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Alerta(rs.getLong(1), rs.getTimestamp(2).toLocalDateTime(), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)));
            }
        }
        return lista;
    }

    public boolean resolver(Connection con, long idAlerta, int idUsuario, String observaciones)
            throws SQLException {
        String sql = "UPDATE alerta SET estado = 'RESUELTA', id_usuario_resuelve = ?, fecha_resolucion = NOW(), "
                   + "observaciones = ? WHERE id_alerta = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, observaciones);
            ps.setLong(3, idAlerta);
            return ps.executeUpdate() == 1;
        }
    }
}
