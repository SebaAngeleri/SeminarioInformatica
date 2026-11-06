package com.vigilplus.sigma.modelo;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class PadronLocalTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 12, 0);

    private static EntradaPadron entrada(String codigo, Credencial.Estado estado, int... puntos) {
        return new EntradaPadron(codigo, "Titular " + codigo, Credencial.Tipo.RFID, estado,
                AHORA.minusDays(30), AHORA.plusDays(30), puntos);
    }

    private final PadronLocal padron = new PadronLocal(List.of(
            entrada("RFID-0005", Credencial.Estado.SUSPENDIDA, 1),
            entrada("RFID-0001", Credencial.Estado.ACTIVA, 2, 1),
            entrada("QR-V-AAAA", Credencial.Estado.ACTIVA, 3),
            entrada("RFID-0002", Credencial.Estado.ACTIVA, 1)));

    @Test
    public void up17_elPadronQuedaOrdenadoPorCodigo() {
        EntradaPadron[] e = padron.entradas();
        for (int i = 1; i < e.length; i++) {
            assertTrue(e[i - 1].codigo().compareTo(e[i].codigo()) < 0);
        }
    }

    @Test
    public void up18_busquedaBinaria_encuentraExtremosYNoExistentes() {
        assertEquals("QR-V-AAAA", padron.buscar("QR-V-AAAA").codigo());
        assertEquals("RFID-0005", padron.buscar("RFID-0005").codigo());
        assertNull(padron.buscar("RFID-0003"));
        assertNull(new PadronLocal(List.of()).buscar("X"));
    }

    @Test
    public void up19_verificacionOffline_usaLasMismasReglasQueElServidor() {
        assertNull(padron.verificar("RFID-0001", 2, AHORA));
        assertEquals(MotivoRechazo.PUNTO_NO_HABILITADO, padron.verificar("RFID-0001", 3, AHORA));
        assertEquals(MotivoRechazo.CREDENCIAL_NO_ACTIVA, padron.verificar("RFID-0005", 1, AHORA));
        assertEquals(MotivoRechazo.CREDENCIAL_VENCIDA, padron.verificar("RFID-0002", 1, AHORA.plusDays(31)));
        assertEquals(MotivoRechazo.CREDENCIAL_INEXISTENTE, padron.verificar("RFID-9999", 1, AHORA));
    }

    @Test
    public void up20_entradaSeReconstruyeDesdeSusPropiosCampos() throws DatoInvalidoException {
        EntradaPadron original = entrada("RFID-0001", Credencial.Estado.ACTIVA, 4, 1, 2);
        assertEquals(original, EntradaPadron.desdeCampos(original.valores(), 2));
    }

    @Test
    public void up21_lineaConFechaInvalida_informaElNumeroDeLinea() {
        String[] campos = entrada("RFID-0001", Credencial.Estado.ACTIVA, 1).valores();
        campos[5] = "31/12/2026";
        try {
            EntradaPadron.desdeCampos(campos, 7);
            fail("Se esperaba DatoInvalidoException");
        } catch (DatoInvalidoException e) {
            assertTrue(e.getMessage().startsWith("Linea 7"));
        }
    }

    @Test
    public void up22_elArregloDePuntosNoPuedeModificarseDesdeAfuera() {
        int[] puntos = {1, 2};
        EntradaPadron e = entrada("X", Credencial.Estado.ACTIVA, puntos);
        puntos[0] = 99;
        e.puntos()[1] = 99;
        assertArrayEquals(new int[] {1, 2}, e.puntos());
    }
}
