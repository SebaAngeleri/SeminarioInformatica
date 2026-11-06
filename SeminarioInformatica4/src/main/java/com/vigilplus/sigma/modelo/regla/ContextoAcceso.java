package com.vigilplus.sigma.modelo.regla;

import java.time.LocalDateTime;

/** Datos del intento de acceso que las reglas necesitan para decidir. */
public record ContextoAcceso(LocalDateTime momento, boolean habilitadaParaPunto) { }
