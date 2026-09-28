package com.vigilplus.sigma.modelo;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/** Pruebas unitarias de la regla de negocio de verificacion de credenciales (caja blanca). */
public class CredencialTest {

    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime HASTA = LocalDateTime.of(2026, 12, 31, 23, 59);
    private static final LocalDateTime DENTRO = LocalDateTime.of(2026, 6, 15, 10, 0);

    private Credencial credencial(Credencial.Estado estado) {
        return new Credencial(1, 1, "RFID-TEST", Credencial.Tipo.RFID, DESDE, HASTA, estado, false);
    }

    @Test
    public void up01_credencialActivaVigenteYHabilitada_seAutoriza() {
        assertNull(credencial(Credencial.Estado.ACTIVA).evaluar(DENTRO, true));
    }

    @Test
    public void up02_credencialVencida_seRechazaPorVencimiento() {
        assertEquals(MotivoRechazo.CREDENCIAL_VENCIDA,
                credencial(Credencial.Estado.ACTIVA).evaluar(HASTA.plusMinutes(1), true));
    }

    @Test
    public void up03_credencialAunNoVigente_seRechazaPorVencimiento() {
        assertEquals(MotivoRechazo.CREDENCIAL_VENCIDA,
                credencial(Credencial.Estado.ACTIVA).evaluar(DESDE.minusSeconds(1), true));
    }

    @Test
    public void up04_limitesDeVigenciaInclusivos() {
        Credencial c = credencial(Credencial.Estado.ACTIVA);
        assertTrue(c.estaVigente(DESDE));
        assertTrue(c.estaVigente(HASTA));
    }

    @Test
    public void up05_credencialSuspendida_seRechazaAunqueEsteVigente() {
        assertEquals(MotivoRechazo.CREDENCIAL_NO_ACTIVA,
                credencial(Credencial.Estado.SUSPENDIDA).evaluar(DENTRO, true));
    }

    @Test
    public void up06_credencialNoHabilitadaParaElPunto_seRechaza() {
        assertEquals(MotivoRechazo.PUNTO_NO_HABILITADO,
                credencial(Credencial.Estado.ACTIVA).evaluar(DENTRO, false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void up07_vigenciaInvalida_lanzaExcepcion() {
        new Credencial(1, 1, "X", Credencial.Tipo.QR, HASTA, DESDE, Credencial.Estado.ACTIVA, true);
    }

    @Test
    public void up08_cadaMotivoTieneTipoDeAlertaAsociado() {
        for (MotivoRechazo m : MotivoRechazo.values()) {
            assertNotNull(m.getCodigoAlerta());
        }
    }
}
