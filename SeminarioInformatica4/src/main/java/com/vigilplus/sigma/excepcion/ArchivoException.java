package com.vigilplus.sigma.excepcion;

/** Error al leer o escribir un archivo (exportacion de historial, padron local). */
public class ArchivoException extends SigmaException {

    private static final long serialVersionUID = 1L;

    public ArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
