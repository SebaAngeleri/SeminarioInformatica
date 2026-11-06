package com.vigilplus.sigma.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** Algoritmos de ordenamiento y busqueda sobre arreglos, usados por el padron y los reportes. */
public final class Algoritmos {

    private Algoritmos() { }

    /**
     * Ordenamiento por insercion (estable). Se eligio porque los volumenes del
     * prototipo son chicos (decenas o cientos de elementos) y porque es muy
     * eficiente cuando los datos ya vienen casi ordenados, como ocurre con los
     * resultados de la base, que suelen llegar ordenados por clave.
     */
    public static <T> void ordenarPorInsercion(T[] arreglo, Comparator<? super T> criterio) {
        for (int i = 1; i < arreglo.length; i++) {
            T actual = arreglo[i];
            int j = i - 1;
            while (j >= 0 && criterio.compare(arreglo[j], actual) > 0) {
                arreglo[j + 1] = arreglo[j];
                j--;
            }
            arreglo[j + 1] = actual;
        }
    }

    /**
     * Busqueda secuencial: devuelve el primer elemento de la lista que cumple
     * la condicion, o null si ninguno la cumple. Se usa sobre listas chicas y
     * sin orden, donde la busqueda binaria no es aplicable.
     */
    public static <T> T buscarSecuencial(List<T> lista, Predicate<? super T> condicion) {
        for (T elemento : lista) {
            if (condicion.test(elemento)) return elemento;
        }
        return null;
    }
}
