package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.EventoAcceso;
import com.vigilplus.sigma.modelo.MotivoRechazo;
import com.vigilplus.sigma.modelo.ResultadoAcceso;
import com.vigilplus.sigma.persistencia.AlertaDAO;
import com.vigilplus.sigma.persistencia.CredencialDAO;
import com.vigilplus.sigma.persistencia.EventoAccesoDAO;
import com.vigilplus.sigma.persistencia.PuntoAccesoDAO;
import com.vigilplus.sigma.persistencia.Transaccion;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Controlador del caso de uso "Autorizar acceso" (CU03).
 * Verifica la credencial (CU04, incluido), registra el evento (CU05, incluido)
 * y, si corresponde, genera la alerta (CU06, extension). Todo se ejecuta en
 * una unica transaccion: nunca queda un rechazo sin su alerta.
 */
public class ControladorAcceso {

    private final CredencialDAO credencialDAO = new CredencialDAO();
    private final EventoAccesoDAO eventoDAO = new EventoAccesoDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final PuntoAccesoDAO puntoDAO = new PuntoAccesoDAO();

    public ResultadoAcceso autorizarAcceso(String codigo, int idPunto, EventoAcceso.Sentido sentido)
            throws SQLException {
        LocalDateTime ahora = LocalDateTime.now();

        return Transaccion.ejecutar(con -> {
            puntoDAO.registrarSenal(con, idPunto);
            Credencial credencial = credencialDAO.buscarPorCodigo(con, codigo);

            EventoAcceso evento;
            if (credencial == null) {
                evento = EventoAcceso.deCodigoDesconocido(idPunto, codigo, sentido, ahora);
            } else {
                boolean habilitada = credencialDAO.estaHabilitadaParaPunto(con, credencial.getIdCredencial(), idPunto);
                MotivoRechazo motivo = credencial.evaluar(ahora, habilitada);
                evento = EventoAcceso.deCredencial(idPunto, credencial.getIdCredencial(), sentido, motivo, ahora);
            }

            long idEvento = eventoDAO.registrar(con, evento);
            if (evento.getMotivo() != null) {
                alertaDAO.generarPorEvento(con, evento.getMotivo().getCodigoAlerta(), idEvento, ahora);
            }
            return new ResultadoAcceso(evento.getResultado(), evento.getMotivo(), idEvento);
        });
    }
}
