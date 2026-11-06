package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.EventoAcceso;
import com.vigilplus.sigma.modelo.RegistroHistorial;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO de la entidad EventoAcceso. Hereda de DaoBase para reutilizar el codigo
 * JDBC; sus consultas devuelven filas del historial (RegistroHistorial).
 */
public class EventoAccesoDAO extends DaoBase<RegistroHistorial> {

    @Override
    protected RegistroHistorial mapear(ResultSet rs) throws SQLException {
        return new RegistroHistorial(fecha(rs, "fecha_hora"), rs.getString("persona"), rs.getString("complejo"),
                rs.getString("identificador"), rs.getString("sentido"), rs.getString("resultado"),
                rs.getString("motivo_rechazo"));
    }

    public long registrar(Connection con, EventoAcceso e) throws SQLException {
        long id = insertar(con, "INSERT INTO evento_acceso (id_punto_acceso, id_credencial, codigo_no_reconocido, "
                        + "fecha_hora, sentido, resultado, motivo_rechazo) VALUES (?, ?, ?, ?, ?, ?, ?)",
                e.getIdPuntoAcceso(), e.getIdCredencial(), e.getCodigoNoReconocido(), e.getFechaHora(),
                e.getSentido(), e.getResultado(), e.getMotivo() == null ? null : e.getMotivo().getDescripcion());
        e.setIdEvento(id);
        return id;
    }

    /** RF06: historial filtrado por persona (DNI), complejo y rango de fechas. Filtros nulos se ignoran. */
    public List<RegistroHistorial> historial(Connection con, String dni, Integer idComplejo,
                                             LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT ea.fecha_hora, COALESCE(CONCAT(p.apellido, ', ', p.nombre), "
                   + "       CONCAT('[', ea.codigo_no_reconocido, ']')) AS persona, "
                   + "       c.nombre AS complejo, pa.identificador, ea.sentido, ea.resultado, ea.motivo_rechazo "
                   + "FROM evento_acceso ea "
                   + "JOIN punto_acceso pa ON pa.id_punto_acceso = ea.id_punto_acceso "
                   + "JOIN complejo c ON c.id_complejo = pa.id_complejo "
                   + "LEFT JOIN credencial cr ON cr.id_credencial = ea.id_credencial "
                   + "LEFT JOIN persona p ON p.id_persona = cr.id_persona "
                   + "WHERE (? IS NULL OR p.dni = ?) AND (? IS NULL OR c.id_complejo = ?) "
                   + "  AND ea.fecha_hora BETWEEN ? AND ? "
                   + "ORDER BY ea.fecha_hora";
        return consultarLista(con, sql, dni, dni, idComplejo, idComplejo, desde, hasta);
    }

    /** Indica si hubo un acceso autorizado en el punto dentro de los segundos previos al momento dado. */
    public boolean huboAccesoAutorizadoReciente(Connection con, int idPunto, LocalDateTime momento,
                                                int segundos) throws SQLException {
        return existe(con, "SELECT 1 FROM evento_acceso WHERE id_punto_acceso = ? AND resultado = 'AUTORIZADO' "
                + "AND fecha_hora BETWEEN ? AND ? LIMIT 1", idPunto, momento.minusSeconds(segundos), momento);
    }
}
