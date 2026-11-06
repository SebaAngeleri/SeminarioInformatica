package com.vigilplus.sigma.reporte;

/** Cantidad de alertas pendientes de un complejo. */
public class ConteoComplejo {

    private final String complejo;
    private int cantidad;

    public ConteoComplejo(String complejo) {
        this.complejo = complejo;
    }

    void incrementar() {
        cantidad++;
    }

    public String getComplejo() { return complejo; }
    public int getCantidad() { return cantidad; }
}
