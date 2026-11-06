package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Persona;

import java.sql.*;
import java.util.List;

/** DAO de la entidad Persona. */
public class PersonaDAO extends DaoBase<Persona> implements Dao<Persona, Integer> {

    private static final String SELECT = "SELECT p.id_persona, p.dni, p.nombre, p.apellido, p.email, "
            + "p.telefono, r.nombre AS rol FROM persona p JOIN rol r ON r.id_rol = p.id_rol ";

    @Override
    protected Persona mapear(ResultSet rs) throws SQLException {
        return new Persona(rs.getInt("id_persona"), rs.getString("dni"), rs.getString("nombre"),
                rs.getString("apellido"), rs.getString("email"), rs.getString("telefono"), rs.getString("rol"));
    }

    @Override
    public Persona buscarPorId(Connection con, Integer id) throws SQLException {
        return consultarUno(con, SELECT + "WHERE p.id_persona = ?", id);
    }

    @Override
    public List<Persona> listarTodos(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "ORDER BY p.apellido, p.nombre");
    }

    public Persona buscarPorDni(Connection con, String dni) throws SQLException {
        return consultarUno(con, SELECT + "WHERE p.dni = ?", dni);
    }

    /** Inserta la persona resolviendo el id del rol por su nombre. Falla si el rol no existe. */
    public int insertar(Connection con, Persona p) throws SQLException {
        long id = insertar(con, "INSERT INTO persona (dni, nombre, apellido, email, telefono, id_rol) "
                        + "SELECT ?, ?, ?, ?, ?, id_rol FROM rol WHERE nombre = ?",
                p.getDni(), p.getNombre(), p.getApellido(), p.getEmail(), p.getTelefono(), p.getRol());
        p.setIdPersona((int) id);
        return p.getIdPersona();
    }
}
