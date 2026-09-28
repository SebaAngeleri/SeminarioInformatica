package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.util.Seguridad;

import java.sql.*;

/** DAO de usuarios del sistema. */
public class UsuarioDAO {

    /** Devuelve el usuario si las credenciales son correctas y esta activo; null en caso contrario. */
    public Usuario autenticar(Connection con, String username, String password) throws SQLException {
        String sql = "SELECT id_usuario, username, perfil FROM usuario "
                   + "WHERE username = ? AND password_hash = ? AND activo = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, Seguridad.sha256(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Usuario(rs.getInt(1), rs.getString(2), Usuario.Perfil.valueOf(rs.getString(3)));
            }
        }
    }
}
