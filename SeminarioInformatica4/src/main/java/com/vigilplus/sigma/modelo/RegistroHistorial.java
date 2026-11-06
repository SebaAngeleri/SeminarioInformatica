package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/** Fila del historial de accesos (RF06). */
public record RegistroHistorial(LocalDateTime fechaHora, String persona, String complejo,
                                String puntoAcceso, String sentido, String resultado,
                                String motivo) implements Exportable {

    private static final String[] ENCABEZADOS =
            {"fecha_hora", "persona", "complejo", "punto_acceso", "sentido", "resultado", "motivo"};

    @Override
    public String[] encabezados() {
        return ENCABEZADOS.clone();
    }

    @Override
    public String[] valores() {
        return new String[] {fechaHora.toString(), persona, complejo, puntoAcceso, sentido, resultado,
                motivo == null ? "" : motivo};
    }
}
