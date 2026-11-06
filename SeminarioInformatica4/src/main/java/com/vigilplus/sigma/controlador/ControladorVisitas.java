package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;
import com.vigilplus.sigma.excepcion.PermisoDenegadoException;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.Persona;
import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.persistencia.CredencialDAO;
import com.vigilplus.sigma.persistencia.PersonaDAO;
import com.vigilplus.sigma.persistencia.PuntoAccesoDAO;
import com.vigilplus.sigma.persistencia.Transaccion;
import com.vigilplus.sigma.persistencia.VisitaDAO;
import com.vigilplus.sigma.util.Seguridad;

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
                                      int horas) throws SigmaException {
        if (operador == null) throw new PermisoDenegadoException("Debe iniciar sesion");
        validar(dni, nombre, apellido, anfitrion, horas);
        LocalDateTime ahora = LocalDateTime.now().withNano(0);

        return Transaccion.ejecutar(con -> {
            Persona visitante = personaDAO.buscarPorDni(con, dni);
            if (visitante == null) {
                visitante = new Persona(0, dni, nombre, apellido, null, telefono, "VISITANTE");
                personaDAO.insertar(con, visitante);
            }
            // Flujo alternativo 4a: si el complejo no existe o no tiene puntos activos, la
            // excepcion provoca el rollback y tampoco queda registrada la persona insertada.
            if (!puntoDAO.existeComplejo(con, idComplejo)) {
                throw new DatoInvalidoException("No existe el complejo " + idComplejo);
            }
            List<Integer> puntos = puntoDAO.puntosActivosDeComplejo(con, idComplejo);
            if (puntos.isEmpty()) throw new DatoInvalidoException("El complejo no tiene puntos de acceso activos");

            Credencial qr = new Credencial(0, visitante.getIdPersona(), Seguridad.generarCodigoQr(),
                    Credencial.Tipo.QR, ahora, ahora.plusHours(horas), Credencial.Estado.ACTIVA, true);
            credencialDAO.insertar(con, qr);
            credencialDAO.habilitarPuntos(con, qr.getIdCredencial(), puntos);
            visitaDAO.insertar(con, visitante.getIdPersona(), qr.getIdCredencial(), idComplejo,
                    operador.idUsuario(), anfitrion, motivo);
            return qr;
        });
    }

    private static void validar(String dni, String nombre, String apellido, String anfitrion, int horas)
            throws DatoInvalidoException {
        if (dni == null || !dni.matches("\\d{7,8}")) {
            throw new DatoInvalidoException("El DNI debe tener 7 u 8 digitos, sin puntos");
        }
        if (nombre.isBlank() || apellido.isBlank() || anfitrion.isBlank()) {
            throw new DatoInvalidoException("Nombre, apellido y anfitrion son obligatorios");
        }
        if (horas < 1 || horas > MAX_HORAS_VISITA) {
            throw new DatoInvalidoException("La duracion debe estar entre 1 y " + MAX_HORAS_VISITA + " horas");
        }
    }
}
