package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Alerta;
import com.vigilplus.sigma.modelo.Severidad;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/** DAO de la entidad Alerta (tablas alerta y tipo_alerta, vista v_alerta_detalle). */
public class AlertaDAO extends DaoBase<Alerta> implements Dao<Alerta, Long> {

    private static final String SELECT = "SELECT id_alerta, fecha_hora, tipo, severidad, estado, complejo, "
            + "punto_acceso FROM v_alerta_detalle ";
    private static final String ORDEN = "ORDER BY FIELD(severidad, 'ALTA', 'MEDIA', 'BAJA'), fecha_hora";

    @Override
    protected Alerta mapear(ResultSet rs) throws SQLException {
        return new Alerta(rs.getLong("id_alerta"), fecha(rs, "fecha_hora"), rs.getString("tipo"),
                Severidad.valueOf(rs.getString("severidad")), rs.getString("estado"),
                rs.getString("complejo"), rs.getString("punto_acceso"));
    }

    @Override
    public Alerta buscarPorId(Connection con, Long id) throws SQLException {
        return consultarUno(con, SELECT + "WHERE id_alerta = ?", id);
    }

    @Override
    public List<Alerta> listarTodos(Connection con) throws SQLException {
        return consultarLista(con, SELECT + ORDEN);
    }

    public List<Alerta> listarPendientes(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "WHERE estado = 'PENDIENTE' " + ORDEN);
    }

    /** Alerta originada en un evento de acceso rechazado. */
    public long generarPorEvento(Connection con, String codigoTipo, long idEvento, LocalDateTime fecha)
            throws SQLException {
        return generar(con, codigoTipo, idEvento, null, fecha);
    }

    /** Alerta originada en una lectura de sensor de puerta. */
    public long generarPorSensor(Connection con, String codigoTipo, long idEventoSensor, LocalDateTime fecha)
            throws SQLException {
        return generar(con, codigoTipo, null, idEventoSensor, fecha);
    }

    private long generar(Connection con, String codigoTipo, Long idEvento, Long idEventoSensor,
                         LocalDateTime fecha) throws SQLException {
        try {
            return insertar(con, "INSERT INTO alerta (id_tipo_alerta, id_evento, id_evento_sensor, fecha_hora) "
                    + "SELECT id_tipo_alerta, ?, ?, ? FROM tipo_alerta WHERE codigo = ?",
                    idEvento, idEventoSensor, fecha, codigoTipo);
        } catch (SQLException e) {
            if ("02000".equals(e.getSQLState())) {
                throw new SQLException("Tipo de alerta inexistente: " + codigoTipo, "02000", e);
            }
            throw e;
        }
    }

    public boolean existeParaEventoSensor(Connection con, long idEventoSensor, String codigoTipo)
            throws SQLException {
        return existe(con, "SELECT 1 FROM alerta a JOIN tipo_alerta t ON t.id_tipo_alerta = a.id_tipo_alerta "
                + "WHERE a.id_evento_sensor = ? AND t.codigo = ?", idEventoSensor, codigoTipo);
    }

    public boolean resolver(Connection con, long idAlerta, int idUsuario, String observaciones)
            throws SQLException {
        return actualizar(con, "UPDATE alerta SET estado = 'RESUELTA', id_usuario_resuelve = ?, "
                + "fecha_resolucion = NOW(), observaciones = ? WHERE id_alerta = ? AND estado = 'PENDIENTE'",
                idUsuario, observaciones, idAlerta) == 1;
    }
}
