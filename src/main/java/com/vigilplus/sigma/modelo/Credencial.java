package com.vigilplus.sigma.modelo;

import java.time.LocalDate;

/**
 * Representa una credencial (RFID o QR) asociada a una persona,
 * de acuerdo con el diagrama de dominio definido en el análisis
 * del proyecto SIGMA.
 */
public class Credencial {

    private int idCredencial;
    private int idPersona;
    private String codigo;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;

    public Credencial(int idCredencial, int idPersona, String codigo,
                       LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        this.idCredencial = idCredencial;
        this.idPersona = idPersona;
        this.codigo = codigo;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
    }

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(vigenciaDesde) && !fecha.isAfter(vigenciaHasta);
    }

    public int getIdCredencial() {
        return idCredencial;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDate getVigenciaDesde() {
        return vigenciaDesde;
    }

    public LocalDate getVigenciaHasta() {
        return vigenciaHasta;
    }
}
