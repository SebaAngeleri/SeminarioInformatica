package com.vigilplus.sigma.excepcion;

/**
 * Raiz de la jerarquia de excepciones propias de SIGMA.
 * Es una excepcion verificada (checked): el compilador obliga a la vista a
 * tratar cada error de negocio o de persistencia, en lugar de que el programa
 * termine de forma inesperada.
 */
public class SigmaException extends Exception {

    private static final long serialVersionUID = 1L;

    public SigmaException(String mensaje) {
        super(mensaje);
    }

    public SigmaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
