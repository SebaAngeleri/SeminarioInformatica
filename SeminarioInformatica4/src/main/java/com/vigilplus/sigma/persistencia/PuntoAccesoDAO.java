package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.EstadoPunto;

import java.sql.*;
import java.util.List;

/** DAO de la entidad PuntoAcceso. */
public class PuntoAccesoDAO extends DaoBase<EstadoPunto> implements Dao<EstadoPunto, Integer> {

    /** Umbral a partir del cual un punto sin senal se considera fuera de linea. */
    public static final int MINUTOS_SIN_SENAL = 5;

    private static final String SELECT = "SELECT pa.id_punto_acceso, c.nombre AS complejo, pa.identificador, "
            + "pa.estado, (pa.ultima_senal >= NOW() - INTERVAL " + MINUTOS_SIN_SENAL + " MINUTE) AS en_linea "
            + "FROM punto_acceso pa JOIN complejo c ON c.id_complejo = pa.id_complejo ";

    @Override
    protected EstadoPunto mapear(ResultSet rs) throws SQLException {
        return new EstadoPunto(rs.getInt("id_punto_acceso"), rs.getString("complejo"),
                rs.getString("identificador"), rs.getString("estado"), rs.getBoolean("en_linea"));
    }

    @Override
    public EstadoPunto buscarPorId(Connection con, Integer id) throws SQLException {
        return consultarUno(con, SELECT + "WHERE pa.id_punto_acceso = ?", id);
    }

    /** RF08: estado en linea de todos los puntos de acceso. */
    @Override
    public List<EstadoPunto> listarTodos(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "ORDER BY c.nombre, pa.identificador");
    }

    /** Actualiza la ultima senal recibida (heartbeat del controlador local). */
    public void registrarSenal(Connection con, int idPunto) throws SQLException {
        actualizar(con, "UPDATE punto_acceso SET ultima_senal = NOW() WHERE id_punto_acceso = ?", idPunto);
    }

    /** Puntos de acceso activos de un complejo (para habilitar credenciales de visitantes). */
    public List<Integer> puntosActivosDeComplejo(Connection con, int idComplejo) throws SQLException {
        return consultar(con, "SELECT id_punto_acceso FROM punto_acceso WHERE id_complejo = ? AND estado = 'ACTIVO'",
                rs -> rs.getInt(1), idComplejo);
    }

    public boolean existeComplejo(Connection con, int idComplejo) throws SQLException {
        return existe(con, "SELECT 1 FROM complejo WHERE id_complejo = ?", idComplejo);
    }
}
