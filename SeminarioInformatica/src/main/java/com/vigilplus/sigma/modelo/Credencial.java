package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/**
 * Credencial (RFID o QR) asociada a una persona. Contiene la regla de
 * negocio central del caso de uso "Autorizar acceso": evaluar si la
 * credencial puede utilizarse en un momento y punto de acceso dados.
 */
public class Credencial {

    public enum Tipo { RFID, QR }
    public enum Estado { ACTIVA, SUSPENDIDA, BAJA }

    private int idCredencial;
    private final int idPersona;
    private final String codigo;
    private final Tipo tipo;
    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;
    private Estado estado;
    private final boolean temporal;

    public Credencial(int idCredencial, int idPersona, String codigo, Tipo tipo,
                      LocalDateTime vigenciaDesde, LocalDateTime vigenciaHasta,
                      Estado estado, boolean temporal) {
        if (!vigenciaHasta.isAfter(vigenciaDesde)) {
            throw new IllegalArgumentException("La vigencia hasta debe ser posterior a la vigencia desde");
        }
        this.idCredencial = idCredencial;
        this.idPersona = idPersona;
        this.codigo = codigo;
        this.tipo = tipo;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
        this.estado = estado;
        this.temporal = temporal;
    }

    public boolean estaVigente(LocalDateTime momento) {
        return !momento.isBefore(vigenciaDesde) && !momento.isAfter(vigenciaHasta);
    }

    /**
     * Evalua la credencial. Devuelve null si el acceso debe autorizarse o
     * el motivo de rechazo en caso contrario. El orden de las validaciones
     * define que motivo se informa cuando fallan varias a la vez.
     */
    public MotivoRechazo evaluar(LocalDateTime momento, boolean habilitadaParaPunto) {
        if (estado != Estado.ACTIVA) return MotivoRechazo.CREDENCIAL_NO_ACTIVA;
        if (!estaVigente(momento))   return MotivoRechazo.CREDENCIAL_VENCIDA;
        if (!habilitadaParaPunto)    return MotivoRechazo.PUNTO_NO_HABILITADO;
        return null;
    }

    public int getIdCredencial() { return idCredencial; }
    public void setIdCredencial(int idCredencial) { this.idCredencial = idCredencial; }
    public int getIdPersona() { return idPersona; }
    public String getCodigo() { return codigo; }
    public Tipo getTipo() { return tipo; }
    public LocalDateTime getVigenciaDesde() { return vigenciaDesde; }
    public LocalDateTime getVigenciaHasta() { return vigenciaHasta; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public boolean isTemporal() { return temporal; }
}
