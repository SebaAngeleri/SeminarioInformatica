package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/**
 * Evento de ingreso o egreso registrado en un punto de acceso.
 * Si el codigo leido no corresponde a ninguna credencial, idCredencial
 * es null y se conserva el codigo en codigoNoReconocido.
 */
public class EventoAcceso {

    public enum Sentido { INGRESO, EGRESO }
    public enum Resultado { AUTORIZADO, RECHAZADO }

    private long idEvento;
    private final int idPuntoAcceso;
    private final Integer idCredencial;
    private final String codigoNoReconocido;
    private final LocalDateTime fechaHora;
    private final Sentido sentido;
    private final Resultado resultado;
    private final MotivoRechazo motivo;

    private EventoAcceso(int idPuntoAcceso, Integer idCredencial, String codigoNoReconocido,
                         LocalDateTime fechaHora, Sentido sentido, MotivoRechazo motivo) {
        this.idPuntoAcceso = idPuntoAcceso;
        this.idCredencial = idCredencial;
        this.codigoNoReconocido = codigoNoReconocido;
        this.fechaHora = fechaHora;
        this.sentido = sentido;
        this.motivo = motivo;
        this.resultado = (motivo == null) ? Resultado.AUTORIZADO : Resultado.RECHAZADO;
    }

    /** Evento de una credencial registrada (autorizado si motivo es null). */
    public static EventoAcceso deCredencial(int idPunto, int idCredencial, Sentido sentido,
                                            MotivoRechazo motivo, LocalDateTime fechaHora) {
        return new EventoAcceso(idPunto, idCredencial, null, fechaHora, sentido, motivo);
    }

    /** Evento de un codigo que no existe en el sistema (siempre rechazado). */
    public static EventoAcceso deCodigoDesconocido(int idPunto, String codigo, Sentido sentido,
                                                   LocalDateTime fechaHora) {
        return new EventoAcceso(idPunto, null, codigo, fechaHora, sentido,
                MotivoRechazo.CREDENCIAL_INEXISTENTE);
    }

    public long getIdEvento() { return idEvento; }
    public void setIdEvento(long idEvento) { this.idEvento = idEvento; }
    public int getIdPuntoAcceso() { return idPuntoAcceso; }
    public Integer getIdCredencial() { return idCredencial; }
    public String getCodigoNoReconocido() { return codigoNoReconocido; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Sentido getSentido() { return sentido; }
    public Resultado getResultado() { return resultado; }
    public MotivoRechazo getMotivo() { return motivo; }
}
