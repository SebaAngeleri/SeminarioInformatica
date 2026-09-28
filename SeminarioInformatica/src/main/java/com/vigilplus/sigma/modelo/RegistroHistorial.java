package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/** Fila del historial de accesos (RF06). */
public record RegistroHistorial(LocalDateTime fechaHora, String persona, String complejo,
                                String puntoAcceso, String sentido, String resultado,
                                String motivo) { }
