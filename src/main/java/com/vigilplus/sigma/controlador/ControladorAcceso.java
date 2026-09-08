package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.EventoAcceso;
import com.vigilplus.sigma.persistencia.CredencialDAO;
import com.vigilplus.sigma.persistencia.EventoAccesoDAO;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controlador (patrón MVC) que implementa el caso de uso
 * "Autorizar acceso" descripto en la sección 8.2 del informe.
 *
 * Orquesta la verificación de la credencial, el registro del
 * evento correspondiente y, si aplica, la generación de una
 * alerta ante un intento de acceso rechazado.
 */
public class ControladorAcceso {

    private final CredencialDAO credencialDAO = new CredencialDAO();
    private final EventoAccesoDAO eventoDAO = new EventoAccesoDAO();

    public EventoAcceso.Resultado autorizarAcceso(String codigoCredencial, int idPuntoAcceso,
                                                    EventoAcceso.Sentido sentido) throws SQLException {

        Credencial credencial = credencialDAO.buscarPorCodigo(codigoCredencial);

        if (credencial == null) {
            registrarRechazo(null, idPuntoAcceso, sentido, "Credencial inexistente");
            return EventoAcceso.Resultado.RECHAZADO;
        }

        boolean vigente = credencial.estaVigente(LocalDate.now());
        boolean habilitada = credencialDAO.estaHabilitadaParaPunto(credencial.getIdCredencial(), idPuntoAcceso);

        if (!vigente || !habilitada) {
            registrarRechazo(credencial.getIdCredencial(), idPuntoAcceso, sentido,
                    !vigente ? "Credencial vencida" : "Credencial no habilitada para este punto de acceso");
            return EventoAcceso.Resultado.RECHAZADO;
        }

        EventoAcceso evento = new EventoAcceso(credencial.getIdCredencial(), idPuntoAcceso, sentido,
                EventoAcceso.Resultado.AUTORIZADO);
        eventoDAO.registrar(evento);
        return EventoAcceso.Resultado.AUTORIZADO;
    }

    private void registrarRechazo(Integer idCredencial, int idPuntoAcceso,
                                    EventoAcceso.Sentido sentido, String motivo) throws SQLException {

        long idEvento = -1;
        if (idCredencial != null) {
            EventoAcceso evento = new EventoAcceso(idCredencial, idPuntoAcceso, sentido,
                    EventoAcceso.Resultado.RECHAZADO);
            idEvento = eventoDAO.registrar(evento);
        }
        eventoDAO.generarAlerta(idPuntoAcceso, idEvento >= 0 ? idEvento : null, "ACCESO_RECHAZADO", motivo);
    }
}
