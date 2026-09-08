package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/**
 * Representa un evento de ingreso o egreso registrado en un
 * punto de acceso, ya sea autorizado o rechazado.
 */
public class EventoAcceso {

    public enum Sentido { INGRESO, EGRESO }
    public enum Resultado { AUTORIZADO, RECHAZADO }

    private long idEvento;
    private int idCredencial;
    private int idPuntoAcceso;
    private LocalDateTime fechaHora;
    private Sentido sentido;
    private Resultado resultado;

    public EventoAcceso(int idCredencial, int idPuntoAcceso, Sentido sentido, Resultado resultado) {
        this.idCredencial = idCredencial;
        this.idPuntoAcceso = idPuntoAcceso;
        this.fechaHora = LocalDateTime.now();
        this.sentido = sentido;
        this.resultado = resultado;
    }

    public long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(long idEvento) {
        this.idEvento = idEvento;
    }

    public int getIdCredencial() {
        return idCredencial;
    }

    public int getIdPuntoAcceso() {
        return idPuntoAcceso;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Sentido getSentido() {
        return sentido;
    }

    public Resultado getResultado() {
        return resultado;
    }
}
