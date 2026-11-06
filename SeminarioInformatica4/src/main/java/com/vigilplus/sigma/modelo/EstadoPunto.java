package com.vigilplus.sigma.modelo;

/** Estado en linea de un punto de acceso (RF08). */
public record EstadoPunto(int idPuntoAcceso, String complejo, String identificador,
                          String estado, boolean enLinea) { }
