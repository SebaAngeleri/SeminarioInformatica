package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.EstadoPunto;
import com.vigilplus.sigma.modelo.RegistroHistorial;
import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.modelo.Visita;
import com.vigilplus.sigma.persistencia.EventoAccesoDAO;
import com.vigilplus.sigma.persistencia.PuntoAccesoDAO;
import com.vigilplus.sigma.persistencia.Transaccion;
import com.vigilplus.sigma.persistencia.UsuarioDAO;
import com.vigilplus.sigma.persistencia.VisitaDAO;

import java.time.LocalDateTime;
import java.util.List;

/** Controlador de consultas: historial (RF06), estado de puntos (RF08), visitas e inicio de sesion. */
public class ControladorConsultas {

    private final EventoAccesoDAO eventoDAO = new EventoAccesoDAO();
    private final PuntoAccesoDAO puntoDAO = new PuntoAccesoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VisitaDAO visitaDAO = new VisitaDAO();

    /** Devuelve el usuario autenticado, o null si el usuario o la clave son incorrectos. */
    public Usuario iniciarSesion(String username, String password) throws SigmaException {
        if (username.isBlank() || password.isBlank()) return null;
        return Transaccion.consultar(con -> usuarioDAO.autenticar(con, username, password));
    }

    public List<RegistroHistorial> historial(String dni, Integer idComplejo, LocalDateTime desde,
                                             LocalDateTime hasta) throws SigmaException {
        if (hasta.isBefore(desde)) {
            throw new DatoInvalidoException("Rango de fechas invalido: la fecha final es anterior a la inicial");
        }
        return Transaccion.consultar(con -> eventoDAO.historial(con, dni, idComplejo, desde, hasta));
    }

    public List<EstadoPunto> estadoPuntos() throws SigmaException {
        return Transaccion.consultar(puntoDAO::listarTodos);
    }

    public List<Visita> visitasVigentes() throws SigmaException {
        return Transaccion.consultar(visitaDAO::listarVigentes);
    }
}
