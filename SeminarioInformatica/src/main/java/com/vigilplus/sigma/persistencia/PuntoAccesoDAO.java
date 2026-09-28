package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.EstadoPunto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO de la entidad PuntoAcceso. */
public class PuntoAccesoDAO {

    /** Umbral a partir del cual un punto sin senal se considera fuera de linea. */
    public static final int MINUTOS_SIN_SENAL = 5;

    /** RF08: estado en linea de todos los puntos de acceso. */
    public List<EstadoPunto> listarEstado(Connection con) throws SQLException {
        String sql = "SELECT pa.id_punto_acceso, c.nombre, pa.identificador, pa.estado, "
                   + "(pa.ultima_senal >= NOW() - INTERVAL " + MINUTOS_SIN_SENAL + " MINUTE) AS en_linea "
                   + "FROM punto_acceso pa JOIN complejo c ON c.id_complejo = pa.id_complejo "
                   + "ORDER BY c.nombre, pa.identificador";
        List<EstadoPunto> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new EstadoPunto(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getBoolean(5)));
            }
        }
        return lista;
    }

    /** Actualiza la ultima senal recibida (heartbeat del controlador local). */
    public void registrarSenal(Connection con, int idPunto) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE punto_acceso SET ultima_senal = NOW() WHERE id_punto_acceso = ?")) {
            ps.setInt(1, idPunto);
            ps.executeUpdate();
        }
    }

    /** Puntos de acceso activos de un complejo (para habilitar credenciales de visitantes). */
    public List<Integer> puntosActivosDeComplejo(Connection con, int idComplejo) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_punto_acceso FROM punto_acceso WHERE id_complejo = ? AND estado = 'ACTIVO'")) {
            ps.setInt(1, idComplejo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }
}
