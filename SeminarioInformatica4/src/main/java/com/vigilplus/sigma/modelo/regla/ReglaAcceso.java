package com.vigilplus.sigma.modelo.regla;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.MotivoRechazo;

/**
 * Regla que una credencial debe cumplir para autorizar un acceso.
 * Clase abstracta: define el comportamiento comun (evaluar) y obliga a cada
 * subclase a implementar la condicion concreta (cumple) y el motivo de
 * rechazo que corresponde cuando la condicion no se cumple.
 */
public abstract class ReglaAcceso {

    private final String nombre;

    protected ReglaAcceso(String nombre) {
        this.nombre = nombre;
    }

    /** Condicion propia de cada regla. */
    protected abstract boolean cumple(Credencial credencial, ContextoAcceso contexto);

    /** Motivo informado cuando la regla no se cumple. */
    public abstract MotivoRechazo motivoRechazo();

    /** Devuelve null si la regla se cumple o el motivo de rechazo en caso contrario. */
    public final MotivoRechazo evaluar(Credencial credencial, ContextoAcceso contexto) {
        return cumple(credencial, contexto) ? null : motivoRechazo();
    }

    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre; }
}
