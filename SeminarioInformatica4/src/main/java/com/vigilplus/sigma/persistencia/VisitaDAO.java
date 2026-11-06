package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Visita;

import java.sql.*;
import java.util.List;

/** DAO de la entidad Visita (acceso temporal, RF07). */
public class VisitaDAO extends DaoBase<Visita> implements Dao<Visita, Integer> {

    private static final String SELECT = "SELECT v.id_visita, CONCAT(p.apellido, ', ', p.nombre) AS visitante, "
            + "co.nombre AS complejo, v.anfitrion, v.motivo, c.codigo, c.vigencia_hasta "
            + "FROM visita v "
            + "JOIN persona p ON p.id_persona = v.id_persona "
            + "JOIN credencial c ON c.id_credencial = v.id_credencial "
            + "JOIN complejo co ON co.id_complejo = v.id_complejo ";

    @Override
    protected Visita mapear(ResultSet rs) throws SQLException {
        return new Visita(rs.getInt("id_visita"), rs.getString("visitante"), rs.getString("complejo"),
                rs.getString("anfitrion"), rs.getString("motivo"), rs.getString("codigo"),
                fecha(rs, "vigencia_hasta"));
    }

    @Override
    public Visita buscarPorId(Connection con, Integer id) throws SQLException {
        return consultarUno(con, SELECT + "WHERE v.id_visita = ?", id);
    }

    @Override
    public List<Visita> listarTodos(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "ORDER BY v.fecha_registro DESC");
    }

    /** Visitas cuya credencial temporal sigue vigente (personas que pueden estar dentro del complejo). */
    public List<Visita> listarVigentes(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "WHERE c.vigencia_hasta >= NOW() AND c.estado = 'ACTIVA' "
                + "ORDER BY c.vigencia_hasta");
    }

    public int insertar(Connection con, int idPersona, int idCredencial, int idComplejo,
                        int idUsuario, String anfitrion, String motivo) throws SQLException {
        return (int) insertar(con, "INSERT INTO visita (id_persona, id_credencial, id_complejo, id_usuario_registra, "
                + "anfitrion, motivo) VALUES (?, ?, ?, ?, ?, ?)",
                idPersona, idCredencial, idComplejo, idUsuario, anfitrion, motivo);
    }
}
