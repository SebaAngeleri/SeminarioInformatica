package com.vigilplus.sigma.modelo;

import com.vigilplus.sigma.modelo.regla.ContextoAcceso;
import com.vigilplus.sigma.modelo.regla.ReglaAcceso;
import com.vigilplus.sigma.modelo.regla.ReglaEstadoActivo;
import com.vigilplus.sigma.modelo.regla.ReglaPuntoHabilitado;
import com.vigilplus.sigma.modelo.regla.ReglaVigencia;

import java.time.LocalDateTime;

/**
 * Credencial (RFID o QR) asociada a una persona. Contiene la regla de
 * negocio central del caso de uso "Autorizar acceso": evaluar si la
 * credencial puede utilizarse en un momento y punto de acceso dados.
 */
public class Credencial {

    public enum Tipo { RFID, QR }
    public enum Estado { ACTIVA, SUSPENDIDA, BAJA }

    /**
     * Reglas que se aplican, en este orden, para autorizar un acceso.
     * Se usa un arreglo porque el conjunto de reglas es fijo y el orden
     * define que motivo se informa cuando fallan varias a la vez.
     */
    private static final ReglaAcceso[] REGLAS = {
            new ReglaEstadoActivo(),
            new ReglaVigencia(),
            new ReglaPuntoHabilitado()
    };

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
     * Cada regla se invoca a traves de la clase abstracta ReglaAcceso
     * (polimorfismo): este metodo no conoce que comprueba cada una.
     */
    public MotivoRechazo evaluar(LocalDateTime momento, boolean habilitadaParaPunto) {
        ContextoAcceso contexto = new ContextoAcceso(momento, habilitadaParaPunto);
        for (ReglaAcceso regla : reglas()) {
            MotivoRechazo motivo = regla.evaluar(this, contexto);
            if (motivo != null) return motivo;
        }
        return null;
    }

    /** Reglas aplicables. Una subclase podria redefinirlo para agregar reglas propias. */
    protected ReglaAcceso[] reglas() {
        return REGLAS;
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
