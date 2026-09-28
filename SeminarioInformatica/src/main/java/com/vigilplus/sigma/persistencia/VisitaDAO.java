package com.vigilplus.sigma.persistencia;

import java.sql.*;

/** DAO de la entidad Visita (acceso temporal, RF07). */
public class VisitaDAO {

    public int insertar(Connection con, int idPersona, int idCredencial, int idComplejo,
                        int idUsuario, String anfitrion, String motivo) throws SQLException {
        String sql = "INSERT INTO visita (id_persona, id_credencial, id_complejo, id_usuario_registra, "
                   + "anfitrion, motivo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPersona);
            ps.setInt(2, idCredencial);
            ps.setInt(3, idComplejo);
            ps.setInt(4, idUsuario);
            ps.setString(5, anfitrion);
            ps.setString(6, motivo);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }
}
