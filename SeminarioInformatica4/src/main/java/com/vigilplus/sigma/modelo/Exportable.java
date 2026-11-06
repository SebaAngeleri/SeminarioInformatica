package com.vigilplus.sigma.modelo;

/**
 * Objeto que puede guardarse como una fila de un archivo de texto (CSV).
 * Cada implementacion decide sus columnas; el exportador trabaja con
 * cualquier Exportable sin conocer su clase concreta (polimorfismo).
 */
public interface Exportable {

    /** Nombres de las columnas, en el mismo orden que valores(). */
    String[] encabezados();

    /** Valores de la fila. La longitud del arreglo es la misma que la de encabezados(). */
    String[] valores();
}
