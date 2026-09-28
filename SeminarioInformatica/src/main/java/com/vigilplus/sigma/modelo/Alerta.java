package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/** Alerta generada ante una condicion anomala (vista v_alerta_detalle). */
public record Alerta(long idAlerta, LocalDateTime fechaHora, String tipo, String severidad,
                     String estado, String complejo, String puntoAcceso) { }
