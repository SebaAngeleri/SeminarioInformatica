package com.vigilplus.sigma.excepcion;

/** El usuario no inicio sesion o su perfil no le permite realizar la operacion. */
public class PermisoDenegadoException extends SigmaException {

    private static final long serialVersionUID = 1L;

    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
