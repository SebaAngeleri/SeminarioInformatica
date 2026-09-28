package com.vigilplus.sigma.util;

import org.junit.Test;

import static org.junit.Assert.*;

/** Pruebas unitarias de las utilidades de seguridad. */
public class SeguridadTest {

    @Test
    public void up09_sha256CoincideConValorConocido() {
        // Valor de referencia: SHA2('abc', 256) en MySQL
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                Seguridad.sha256("abc"));
    }

    @Test
    public void up10_codigosQrTienenFormatoEsperadoYNoSeRepiten() {
        String a = Seguridad.generarCodigoQr();
        String b = Seguridad.generarCodigoQr();
        assertTrue(a.matches("QR-V-[A-Z2-9]{8}"));
        assertNotEquals(a, b);
    }
}
