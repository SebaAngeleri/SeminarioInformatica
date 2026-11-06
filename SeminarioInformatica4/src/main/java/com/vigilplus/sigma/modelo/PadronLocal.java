package com.vigilplus.sigma.modelo;

import com.vigilplus.sigma.util.Algoritmos;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Copia en memoria de las credenciales de una locacion, que permite validar
 * accesos cuando el controlador local pierde la conexion con el servidor
 * central (modo store and forward definido en el TP2).
 *
 * Las entradas se guardan en un arreglo ordenado por codigo: el padron no
 * cambia una vez cargado y el orden permite buscar con busqueda binaria,
 * en O(log n) comparaciones, en lugar de recorrerlo completo.
 */
public class PadronLocal {

    private static final Comparator<EntradaPadron> POR_CODIGO = Comparator.comparing(EntradaPadron::codigo);

    private final EntradaPadron[] entradas;

    /** Construye el padron a partir de una lista (por ejemplo, la leida de la base o de un archivo). */
    public PadronLocal(List<EntradaPadron> lista) {
        this.entradas = lista.toArray(new EntradaPadron[0]);
        Algoritmos.ordenarPorInsercion(entradas, POR_CODIGO);
    }

    /** Busqueda binaria por codigo. Devuelve null si el codigo no esta en el padron. */
    public EntradaPadron buscar(String codigo) {
        int inicio = 0;
        int fin = entradas.length - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) >>> 1;
            int comparacion = entradas[medio].codigo().compareTo(codigo);
            if (comparacion == 0) return entradas[medio];
            if (comparacion < 0) inicio = medio + 1;
            else fin = medio - 1;
        }
        return null;
    }

    /**
     * Valida un acceso sin conexion, con las mismas reglas que usa el servidor.
     * @return null si el acceso se autoriza o el motivo del rechazo
     */
    public MotivoRechazo verificar(String codigo, int idPunto, LocalDateTime momento) {
        EntradaPadron entrada = buscar(codigo);
        if (entrada == null) return MotivoRechazo.CREDENCIAL_INEXISTENTE;
        return entrada.comoCredencial().evaluar(momento, entrada.habilitadaEn(idPunto));
    }

    public int tamanio() {
        return entradas.length;
    }

    /** Copia del arreglo interno, ordenada por codigo (encapsulamiento: no se expone el original). */
    public EntradaPadron[] entradas() {
        return entradas.clone();
    }
}
