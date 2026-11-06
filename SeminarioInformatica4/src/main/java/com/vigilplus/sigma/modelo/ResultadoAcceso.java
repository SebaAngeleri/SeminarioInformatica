package com.vigilplus.sigma.modelo;

/** Respuesta del caso de uso "Autorizar acceso" hacia la vista o el controlador local. */
public record ResultadoAcceso(EventoAcceso.Resultado resultado, MotivoRechazo motivo, long idEvento) {
    public boolean autorizado() { return resultado == EventoAcceso.Resultado.AUTORIZADO; }
    public String descripcion() {
        return autorizado() ? "AUTORIZADO" : "RECHAZADO - " + motivo.getDescripcion();
    }
}
