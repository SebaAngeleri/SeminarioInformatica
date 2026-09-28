package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.modelo.EstadoPunto;
import com.vigilplus.sigma.modelo.RegistroHistorial;
import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.persistencia.Conexion;
import com.vigilplus.sigma.persistencia.EventoAccesoDAO;
import com.vigilplus.sigma.persistencia.PuntoAccesoDAO;
import com.vigilplus.sigma.persistencia.UsuarioDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** Controlador de consultas: historial (RF06), estado de puntos (RF08) e inicio de sesion. */
public class ControladorConsultas {

    private final EventoAccesoDAO eventoDAO = new EventoAccesoDAO();
    private final PuntoAccesoDAO puntoDAO = new PuntoAccesoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String username, String password) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            return usuarioDAO.autenticar(con, username, password);
        }
    }

    public List<RegistroHistorial> historial(String dni, Integer idComplejo, LocalDateTime desde,
                                             LocalDateTime hasta) throws SQLException {
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("Rango de fechas invalido");
        try (Connection con = Conexion.obtener()) {
            return eventoDAO.historial(con, dni, idComplejo, desde, hasta);
        }
    }

    public List<EstadoPunto> estadoPuntos() throws SQLException {
        try (Connection con = Conexion.obtener()) {
            return puntoDAO.listarEstado(con);
        }
    }
}
