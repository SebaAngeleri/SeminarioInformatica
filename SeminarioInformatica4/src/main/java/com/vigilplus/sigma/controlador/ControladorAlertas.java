package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;
import com.vigilplus.sigma.excepcion.PermisoDenegadoException;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.Alerta;
import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.persistencia.AlertaDAO;
import com.vigilplus.sigma.persistencia.EventoAccesoDAO;
import com.vigilplus.sigma.persistencia.SensorDAO;
import com.vigilplus.sigma.persistencia.Transaccion;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de los casos de uso "Monitorear sensores de puerta" (CU08),
 * "Generar alerta" (CU06) y "Gestionar alertas" (consulta y resolucion).
 */
public class ControladorAlertas {

    /** Ventana en la que una apertura se considera consecuencia de un acceso autorizado. */
    public static final int VENTANA_APERTURA_SEG = 10;

    /** Estados validos que informa el contacto de la puerta. */
    private static final String[] ESTADOS_PUERTA = {"ABIERTA", "CERRADA"};

    private final SensorDAO sensorDAO = new SensorDAO();
    private final EventoAccesoDAO eventoDAO = new EventoAccesoDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();

    /**
     * Registra una lectura del sensor de puerta. Si la puerta se abre sin un
     * acceso autorizado reciente en ese punto, genera una alerta PUERTA_FORZADA.
     * @return true si se genero una alerta
     */
    public boolean procesarLecturaSensor(int idSensor, String estadoPuerta) throws SigmaException {
        if (!estadoValido(estadoPuerta)) {
            throw new DatoInvalidoException("Estado de puerta invalido: use ABIERTA o CERRADA");
        }
        LocalDateTime ahora = LocalDateTime.now();
        return Transaccion.ejecutar(con -> {
            Integer idPunto = sensorDAO.puntoDelSensor(con, idSensor);
            if (idPunto == null) throw new DatoInvalidoException("No existe el sensor " + idSensor);
            long idLectura = sensorDAO.registrarLectura(con, idSensor, estadoPuerta, ahora);
            if (!"ABIERTA".equals(estadoPuerta)) return false;
            if (eventoDAO.huboAccesoAutorizadoReciente(con, idPunto, ahora, VENTANA_APERTURA_SEG)) return false;
            alertaDAO.generarPorSensor(con, "PUERTA_FORZADA", idLectura, ahora);
            return true;
        });
    }

    /**
     * RF05: tarea periodica (p. ej. cada 5 s) que genera una alerta PUERTA_ABIERTA por cada
     * puerta abierta mas alla de su tiempo maximo. Es idempotente: no duplica alertas.
     * @return cantidad de alertas nuevas
     */
    public int verificarPuertasAbiertas() throws SigmaException {
        LocalDateTime ahora = LocalDateTime.now();
        return Transaccion.ejecutar(con -> {
            int nuevas = 0;
            for (SensorDAO.AperturaExcedida a : sensorDAO.aperturasExcedidas(con, ahora)) {
                if (!alertaDAO.existeParaEventoSensor(con, a.idEventoSensor(), "PUERTA_ABIERTA")) {
                    alertaDAO.generarPorSensor(con, "PUERTA_ABIERTA", a.idEventoSensor(), ahora);
                    nuevas++;
                }
            }
            return nuevas;
        });
    }

    public List<Alerta> pendientes() throws SigmaException {
        return Transaccion.consultar(alertaDAO::listarPendientes);
    }

    public boolean resolver(Usuario usuario, long idAlerta, String observaciones) throws SigmaException {
        if (usuario == null) throw new PermisoDenegadoException("Debe iniciar sesion");
        if (observaciones == null || observaciones.isBlank()) {
            throw new DatoInvalidoException("Indique que se hizo para resolver la alerta");
        }
        return Transaccion.ejecutar(con -> alertaDAO.resolver(con, idAlerta, usuario.idUsuario(), observaciones));
    }

    private static boolean estadoValido(String estado) {
        for (String valido : ESTADOS_PUERTA) {
            if (valido.equals(estado)) return true;
        }
        return false;
    }
}
