package com.vigilplus.sigma.excepcion;

/** El dato ingresado no cumple una regla de negocio (rango, formato o existencia). */
public class DatoInvalidoException extends SigmaException {

    private static final long serialVersionUID = 1L;

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
