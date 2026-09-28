package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Credencial;

import java.sql.*;
import java.util.List;

/** DAO de la entidad Credencial y de la relacion credencial_punto_acceso. */
public class CredencialDAO {

    public Credencial buscarPorCodigo(Connection con, String codigo) throws SQLException {
        String sql = "SELECT id_credencial, id_persona, codigo, tipo, vigencia_desde, vigencia_hasta, "
                   + "estado, temporal FROM credencial WHERE codigo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Credencial(rs.getInt("id_credencial"), rs.getInt("id_persona"),
                        rs.getString("codigo"), Credencial.Tipo.valueOf(rs.getString("tipo")),
                        rs.getTimestamp("vigencia_desde").toLocalDateTime(),
                        rs.getTimestamp("vigencia_hasta").toLocalDateTime(),
                        Credencial.Estado.valueOf(rs.getString("estado")), rs.getBoolean("temporal"));
            }
        }
    }

    public boolean estaHabilitadaParaPunto(Connection con, int idCredencial, int idPunto) throws SQLException {
        String sql = "SELECT 1 FROM credencial_punto_acceso WHERE id_credencial = ? AND id_punto_acceso = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCredencial);
            ps.setInt(2, idPunto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertar(Connection con, Credencial c) throws SQLException {
        String sql = "INSERT INTO credencial (id_persona, codigo, tipo, vigencia_desde, vigencia_hasta, estado, temporal) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getIdPersona());
            ps.setString(2, c.getCodigo());
            ps.setString(3, c.getTipo().name());
            ps.setTimestamp(4, Timestamp.valueOf(c.getVigenciaDesde()));
            ps.setTimestamp(5, Timestamp.valueOf(c.getVigenciaHasta()));
            ps.setString(6, c.getEstado().name());
            ps.setBoolean(7, c.isTemporal());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                c.setIdCredencial(k.getInt(1));
                return c.getIdCredencial();
            }
        }
    }

    public void habilitarPuntos(Connection con, int idCredencial, List<Integer> puntos) throws SQLException {
        String sql = "INSERT INTO credencial_punto_acceso (id_credencial, id_punto_acceso) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int idPunto : puntos) {
                ps.setInt(1, idCredencial);
                ps.setInt(2, idPunto);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int revocarPunto(Connection con, String codigo, int idPunto) throws SQLException {
        String sql = "DELETE cpa FROM credencial_punto_acceso cpa "
                   + "JOIN credencial c ON c.id_credencial = cpa.id_credencial "
                   + "WHERE c.codigo = ? AND cpa.id_punto_acceso = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, idPunto);
            return ps.executeUpdate();
        }
    }

    public int cambiarEstado(Connection con, String codigo, Credencial.Estado estado) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE credencial SET estado = ? WHERE codigo = ?")) {
            ps.setString(1, estado.name());
            ps.setString(2, codigo);
            return ps.executeUpdate();
        }
    }

    public int modificarVigencia(Connection con, String codigo, Timestamp hasta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE credencial SET vigencia_hasta = ? WHERE codigo = ? AND vigencia_desde < ?")) {
            ps.setTimestamp(1, hasta);
            ps.setString(2, codigo);
            ps.setTimestamp(3, hasta);
            return ps.executeUpdate();
        }
    }
}
