package com.vigilplus.sigma.modelo;

/** Usuario autenticado del sistema (operador o administrador). */
public record Usuario(int idUsuario, String username, Perfil perfil) {
    public enum Perfil { OPERADOR, ADMINISTRADOR }
    public boolean esAdministrador() { return perfil == Perfil.ADMINISTRADOR; }
}
