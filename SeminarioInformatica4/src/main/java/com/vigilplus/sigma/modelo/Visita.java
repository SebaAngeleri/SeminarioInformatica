package com.vigilplus.sigma.modelo;

import java.time.LocalDateTime;

/** Visita registrada por un operador, con su credencial QR temporal (RF07). */
public record Visita(int idVisita, String visitante, String complejo, String anfitrion, String motivo,
                     String codigoQr, LocalDateTime venceEl) { }
