package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/** Alerta generada ante una condicion anomala (vista v_alerta_detalle). */
public record Alerta(long idAlerta, LocalDateTime fechaHora, String tipo, Severidad severidad,
                     String estado, String complejo, String puntoAcceso) implements Exportable {

    private static final String[] ENCABEZADOS =
            {"id_alerta", "fecha_hora", "tipo", "severidad", "estado", "complejo", "punto_acceso"};

    @Override
    public String[] encabezados() {
        return ENCABEZADOS.clone();
    }

    @Override
    public String[] valores() {
        return new String[] {String.valueOf(idAlerta), fechaHora.toString(), tipo, severidad.name(),
                estado, complejo, puntoAcceso};
    }
}
