package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.util.Seguridad;

import java.sql.*;
import java.util.List;

/** DAO de usuarios del sistema. */
public class UsuarioDAO extends DaoBase<Usuario> implements Dao<Usuario, Integer> {

    private static final String SELECT = "SELECT id_usuario, username, perfil FROM usuario ";

    @Override
    protected Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id_usuario"), rs.getString("username"),
                Usuario.Perfil.valueOf(rs.getString("perfil")));
    }

    @Override
    public Usuario buscarPorId(Connection con, Integer id) throws SQLException {
        return consultarUno(con, SELECT + "WHERE id_usuario = ?", id);
    }

    @Override
    public List<Usuario> listarTodos(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "ORDER BY username");
    }

    /** Devuelve el usuario si las credenciales son correctas y esta activo; null en caso contrario. */
    public Usuario autenticar(Connection con, String username, String password) throws SQLException {
        return consultarUno(con, SELECT + "WHERE username = ? AND password_hash = ? AND activo = TRUE",
                username, Seguridad.sha256(password));
    }
}
