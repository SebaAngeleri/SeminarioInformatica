package com.vigilplus.sigma.reporte;

import com.vigilplus.sigma.modelo.Alerta;
import com.vigilplus.sigma.modelo.Severidad;
import com.vigilplus.sigma.util.Algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Resumen de las alertas pendientes para el panel del operador.
 * Combina las dos estructuras de forma complementaria:
 *  - un arreglo int[] de tamano fijo, indexado por el ordinal de Severidad,
 *    porque la cantidad de severidades es conocida y no cambia;
 *  - un ArrayList de conteos por complejo, porque la cantidad de complejos
 *    con alertas no se conoce hasta recorrer los datos.
 * Al final, la lista se pasa a un arreglo para ordenarla y obtener el ranking.
 */
public class ResumenAlertas {

    private static final Comparator<ConteoComplejo> MAS_ALERTAS_PRIMERO =
            Comparator.comparingInt(ConteoComplejo::getCantidad).reversed()
                      .thenComparing(ConteoComplejo::getComplejo);

    private final int[] porSeveridad = new int[Severidad.values().length];
    private final ConteoComplejo[] ranking;

    public ResumenAlertas(List<Alerta> alertas) {
        List<ConteoComplejo> conteos = new ArrayList<>();
        for (Alerta alerta : alertas) {
            porSeveridad[alerta.severidad().ordinal()]++;

            ConteoComplejo conteo = Algoritmos.buscarSecuencial(conteos,
                    c -> c.getComplejo().equals(alerta.complejo()));
            if (conteo == null) {
                conteo = new ConteoComplejo(alerta.complejo());
                conteos.add(conteo);
            }
            conteo.incrementar();
        }
        ranking = conteos.toArray(new ConteoComplejo[0]);
        Algoritmos.ordenarPorInsercion(ranking, MAS_ALERTAS_PRIMERO);
    }

    public int cantidad(Severidad severidad) {
        return porSeveridad[severidad.ordinal()];
    }

    public int total() {
        int total = 0;
        for (int cantidad : porSeveridad) {
            total += cantidad;
        }
        return total;
    }

    /** Complejos ordenados de mayor a menor cantidad de alertas pendientes. */
    public ConteoComplejo[] ranking() {
        return ranking.clone();
    }
}
