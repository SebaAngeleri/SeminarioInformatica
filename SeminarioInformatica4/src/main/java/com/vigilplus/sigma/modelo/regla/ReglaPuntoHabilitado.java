package com.vigilplus.sigma.modelo.regla;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.MotivoRechazo;

/** La credencial debe estar habilitada para el punto de acceso donde se presenta. */
public class ReglaPuntoHabilitado extends ReglaAcceso {

    public ReglaPuntoHabilitado() {
        super("Punto habilitado");
    }

    @Override
    protected boolean cumple(Credencial credencial, ContextoAcceso contexto) {
        return contexto.habilitadaParaPunto();
    }

    @Override
    public MotivoRechazo motivoRechazo() {
        return MotivoRechazo.PUNTO_NO_HABILITADO;
    }
}
