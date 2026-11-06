package com.vigilplus.sigma.modelo;

/**
 * Motivos por los que un intento de acceso puede ser rechazado.
 * Cada motivo se asocia al codigo de tipo de alerta que genera
 * (tabla tipo_alerta).
 */
public enum MotivoRechazo {
    CREDENCIAL_INEXISTENTE("Credencial inexistente", "CRED_INEXISTENTE"),
    CREDENCIAL_NO_ACTIVA("Credencial suspendida o dada de baja", "CRED_NO_AUTORIZADA"),
    CREDENCIAL_VENCIDA("Credencial vencida", "CRED_VENCIDA"),
    PUNTO_NO_HABILITADO("Credencial no habilitada para este punto de acceso", "CRED_NO_AUTORIZADA");

    private final String descripcion;
    private final String codigoAlerta;

    MotivoRechazo(String descripcion, String codigoAlerta) {
        this.descripcion = descripcion;
        this.codigoAlerta = codigoAlerta;
    }

    public String getDescripcion() { return descripcion; }
    public String getCodigoAlerta() { return codigoAlerta; }
}
