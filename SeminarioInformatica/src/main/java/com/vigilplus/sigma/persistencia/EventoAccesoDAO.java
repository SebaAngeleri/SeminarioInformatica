package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.EventoAcceso;
import com.vigilplus.sigma.modelo.RegistroHistorial;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de la entidad EventoAcceso. */
public class EventoAccesoDAO {

    public long registrar(Connection con, EventoAcceso e) throws SQLException {
        String sql = "INSERT INTO evento_acceso (id_punto_acceso, id_credencial, codigo_no_reconocido, "
                   + "fecha_hora, sentido, resultado, motivo_rechazo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getIdPuntoAcceso());
            if (e.getIdCredencial() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, e.getIdCredencial());
            ps.setString(3, e.getCodigoNoReconocido());
            ps.setTimestamp(4, Timestamp.valueOf(e.getFechaHora()));
            ps.setString(5, e.getSentido().name());
            ps.setString(6, e.getResultado().name());
            ps.setString(7, e.getMotivo() == null ? null : e.getMotivo().getDescripcion());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                e.setIdEvento(k.getLong(1));
                return e.getIdEvento();
            }
        }
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
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setString(2, dni);
            if (idComplejo == null) { ps.setNull(3, Types.INTEGER); ps.setNull(4, Types.INTEGER); }
            else { ps.setInt(3, idComplejo); ps.setInt(4, idComplejo); }
            ps.setTimestamp(5, Timestamp.valueOf(desde));
            ps.setTimestamp(6, Timestamp.valueOf(hasta));
            List<RegistroHistorial> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RegistroHistorial(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2),
                            rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)));
                }
            }
            return lista;
        }
    }

    /** Indica si hubo un acceso autorizado en el punto dentro de los segundos previos al momento dado. */
    public boolean huboAccesoAutorizadoReciente(Connection con, int idPunto, LocalDateTime momento,
                                                int segundos) throws SQLException {
        String sql = "SELECT 1 FROM evento_acceso WHERE id_punto_acceso = ? AND resultado = 'AUTORIZADO' "
                   + "AND fecha_hora BETWEEN ? AND ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPunto);
            ps.setTimestamp(2, Timestamp.valueOf(momento.minusSeconds(segundos)));
            ps.setTimestamp(3, Timestamp.valueOf(momento));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
