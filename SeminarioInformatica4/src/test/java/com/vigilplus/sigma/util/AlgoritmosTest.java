package com.vigilplus.sigma.util;

import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class AlgoritmosTest {

    @Test
    public void up14_ordenarPorInsercion_ordenaYEsEstable() {
        String[] datos = {"QR-3", "RFID-1", "QR-1", "RFID-1b", "QR-2"};
        Algoritmos.ordenarPorInsercion(datos, Comparator.comparingInt(String::length));
        assertArrayEquals(new String[] {"QR-3", "QR-1", "QR-2", "RFID-1", "RFID-1b"}, datos);
    }

    @Test
    public void up15_ordenarArregloVacioOUnElemento_noFalla() {
        Integer[] vacio = {};
        Integer[] uno = {7};
        Algoritmos.ordenarPorInsercion(vacio, Comparator.naturalOrder());
        Algoritmos.ordenarPorInsercion(uno, Comparator.naturalOrder());
        assertEquals(7, (int) uno[0]);
    }

    @Test
    public void up16_busquedaSecuencial_encuentraElPrimeroONull() {
        List<String> lista = List.of("Torre Libertador", "Edificio Catalinas");
        assertEquals("Edificio Catalinas", Algoritmos.buscarSecuencial(lista, s -> s.startsWith("Edificio")));
        assertNull(Algoritmos.buscarSecuencial(lista, s -> s.startsWith("Complejo")));
    }
}
