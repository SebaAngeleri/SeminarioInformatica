package com.vigilplus.sigma.modelo;

import com.vigilplus.sigma.modelo.regla.*;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/** Pruebas de la jerarquia de reglas: cada subclase responde a evaluar() con su propio criterio. */
public class ReglaAccesoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 6, 15, 10, 0);

    private final Credencial suspendidaYVencida = new Credencial(1, 1, "X", Credencial.Tipo.RFID,
            AHORA.minusDays(10), AHORA.minusDays(1), Credencial.Estado.SUSPENDIDA, false);

    @Test
    public void up11_cadaReglaDevuelveSuPropioMotivo() {
        ContextoAcceso ctx = new ContextoAcceso(AHORA, false);
        ReglaAcceso[] reglas = {new ReglaEstadoActivo(), new ReglaVigencia(), new ReglaPuntoHabilitado()};
        MotivoRechazo[] esperados = {MotivoRechazo.CREDENCIAL_NO_ACTIVA, MotivoRechazo.CREDENCIAL_VENCIDA,
                MotivoRechazo.PUNTO_NO_HABILITADO};
        for (int i = 0; i < reglas.length; i++) {
            assertEquals(reglas[i].getNombre(), esperados[i], reglas[i].evaluar(suspendidaYVencida, ctx));
        }
    }

    @Test
    public void up12_reglaQueSeCumple_devuelveNull() {
        assertNull(new ReglaPuntoHabilitado().evaluar(suspendidaYVencida, new ContextoAcceso(AHORA, true)));
    }

    @Test
    public void up13_siFallanVariasReglas_seInformaLaPrimeraDelArreglo() {
        assertEquals(MotivoRechazo.CREDENCIAL_NO_ACTIVA, suspendidaYVencida.evaluar(AHORA, false));
    }
}
