package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.Persona;
import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.persistencia.CredencialDAO;
import com.vigilplus.sigma.persistencia.PersonaDAO;
import com.vigilplus.sigma.persistencia.PuntoAccesoDAO;
import com.vigilplus.sigma.persistencia.Transaccion;
import com.vigilplus.sigma.persistencia.VisitaDAO;
import com.vigilplus.sigma.util.Seguridad;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador del caso de uso "Registrar acceso temporal de visitante" (CU02, RF07).
 * Crea (o reutiliza) la persona, emite una credencial QR temporal habilitada
 * en los puntos activos del complejo y registra la visita, en una sola transaccion.
 */
public class ControladorVisitas {

    public static final int MAX_HORAS_VISITA = 12;

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final CredencialDAO credencialDAO = new CredencialDAO();
    private final PuntoAccesoDAO puntoDAO = new PuntoAccesoDAO();
    private final VisitaDAO visitaDAO = new VisitaDAO();

    public Credencial registrarVisita(Usuario operador, String dni, String nombre, String apellido,
                                      String telefono, int idComplejo, String anfitrion, String motivo,
                                      int horas) throws SQLException {
        if (operador == null) throw new SecurityException("Debe iniciar sesion");
        if (horas < 1 || horas > MAX_HORAS_VISITA) {
            throw new IllegalArgumentException("La duracion debe estar entre 1 y " + MAX_HORAS_VISITA + " horas");
        }
        LocalDateTime ahora = LocalDateTime.now().withNano(0);

        return Transaccion.ejecutar(con -> {
            Persona visitante = personaDAO.buscarPorDni(con, dni);
            if (visitante == null) {
                visitante = new Persona(0, dni, nombre, apellido, null, telefono, "VISITANTE");
                personaDAO.insertar(con, visitante);
            }
            List<Integer> puntos = puntoDAO.puntosActivosDeComplejo(con, idComplejo);
            if (puntos.isEmpty()) throw new IllegalStateException("El complejo no tiene puntos de acceso activos");

            Credencial qr = new Credencial(0, visitante.getIdPersona(), Seguridad.generarCodigoQr(),
                    Credencial.Tipo.QR, ahora, ahora.plusHours(horas), Credencial.Estado.ACTIVA, true);
            credencialDAO.insertar(con, qr);
            credencialDAO.habilitarPuntos(con, qr.getIdCredencial(), puntos);
            visitaDAO.insertar(con, visitante.getIdPersona(), qr.getIdCredencial(), idComplejo,
                    operador.idUsuario(), anfitrion, motivo);
            return qr;
        });
    }
}
