package com.vigilplus.sigma.modelo.regla;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.MotivoRechazo;

/** La credencial no debe estar suspendida ni dada de baja. */
public class ReglaEstadoActivo extends ReglaAcceso {

    public ReglaEstadoActivo() {
        super("Estado activo");
    }

    @Override
    protected boolean cumple(Credencial credencial, ContextoAcceso contexto) {
        return credencial.getEstado() == Credencial.Estado.ACTIVA;
    }

    @Override
    public MotivoRechazo motivoRechazo() {
        return MotivoRechazo.CREDENCIAL_NO_ACTIVA;
    }
}
