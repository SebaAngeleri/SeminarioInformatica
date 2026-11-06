package com.vigilplus.sigma.modelo.regla;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.MotivoRechazo;

/** El momento del acceso debe estar dentro del periodo de vigencia (limites inclusivos). */
public class ReglaVigencia extends ReglaAcceso {

    public ReglaVigencia() {
        super("Vigencia");
    }

    @Override
    protected boolean cumple(Credencial credencial, ContextoAcceso contexto) {
        return credencial.estaVigente(contexto.momento());
    }

    @Override
    public MotivoRechazo motivoRechazo() {
        return MotivoRechazo.CREDENCIAL_VENCIDA;
    }
}
