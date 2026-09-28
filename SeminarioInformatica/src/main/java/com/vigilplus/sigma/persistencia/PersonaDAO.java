package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Persona;

import java.sql.*;

/** DAO de la entidad Persona. */
public class PersonaDAO {

    public Persona buscarPorDni(Connection con, String dni) throws SQLException {
        String sql = "SELECT p.id_persona, p.dni, p.nombre, p.apellido, p.email, p.telefono, r.nombre AS rol "
                   + "FROM persona p JOIN rol r ON r.id_rol = p.id_rol WHERE p.dni = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Persona(rs.getInt("id_persona"), rs.getString("dni"), rs.getString("nombre"),
                        rs.getString("apellido"), rs.getString("email"), rs.getString("telefono"),
                        rs.getString("rol"));
            }
        }
    }

    public int insertar(Connection con, Persona p) throws SQLException {
        String sql = "INSERT INTO persona (dni, nombre, apellido, email, telefono, id_rol) "
                   + "SELECT ?, ?, ?, ?, ?, id_rol FROM rol WHERE nombre = ?";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getDni());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getApellido());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getTelefono());
            ps.setString(6, p.getRol());
            if (ps.executeUpdate() == 0) throw new SQLException("Rol inexistente: " + p.getRol());
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                p.setIdPersona(k.getInt(1));
                return p.getIdPersona();
            }
        }
    }
}
