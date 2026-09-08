package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.EventoAcceso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Acceso a datos (DAO) para la entidad EventoAcceso y para la
 * generación de alertas asociadas.
 */
public class EventoAccesoDAO {

    public long registrar(EventoAcceso evento) throws SQLException {
        String sql = "INSERT INTO evento_acceso (id_credencial, id_punto_acceso, fecha_hora, sentido, resultado) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, evento.getIdCredencial());
            stmt.setInt(2, evento.getIdPuntoAcceso());
            stmt.setTimestamp(3, Timestamp.valueOf(evento.getFechaHora()));
            stmt.setString(4, evento.getSentido().name());
            stmt.setString(5, evento.getResultado().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    public void generarAlerta(int idPuntoAcceso, Long idEvento, String tipo, String descripcion) throws SQLException {
        String sql = "INSERT INTO alerta (id_punto_acceso, id_evento, tipo, descripcion) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, idPuntoAcceso);
            if (idEvento == null) {
                stmt.setNull(2, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(2, idEvento);
            }
            stmt.setString(3, tipo);
            stmt.setString(4, descripcion);
            stmt.executeUpdate();
        }
    }
}
