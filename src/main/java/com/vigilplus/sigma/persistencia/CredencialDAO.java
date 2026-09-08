package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Credencial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos (DAO) para la entidad Credencial.
 * Encapsula las consultas JDBC necesarias para el caso de uso
 * "Autorizar acceso".
 */
public class CredencialDAO {

    public Credencial buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT id_credencial, id_persona, codigo, fecha_vigencia_desde, fecha_vigencia_hasta "
                   + "FROM credencial WHERE codigo = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Credencial(
                        rs.getInt("id_credencial"),
                        rs.getInt("id_persona"),
                        rs.getString("codigo"),
                        rs.getDate("fecha_vigencia_desde").toLocalDate(),
                        rs.getDate("fecha_vigencia_hasta").toLocalDate()
                );
            }
        }
    }

    public boolean estaHabilitadaParaPunto(int idCredencial, int idPuntoAcceso) throws SQLException {
        String sql = "SELECT 1 FROM credencial_punto_acceso "
                   + "WHERE id_credencial = ? AND id_punto_acceso = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, idCredencial);
            stmt.setInt(2, idPuntoAcceso);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
