package com.vigilplus.sigma.util;

import com.vigilplus.sigma.excepcion.ArchivoException;
import com.vigilplus.sigma.modelo.RegistroHistorial;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class ArchivoCsvTest {

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    @Test
    public void up23_escribirYLeer_conservaCamposConSeparadorYComillas() throws ArchivoException {
        RegistroHistorial r = new RegistroHistorial(LocalDateTime.of(2026, 9, 21, 8, 2, 11), "Sosa; Julieta",
                "Torre \"Libertador\"", "Puerta Principal", "INGRESO", "RECHAZADO", null);
        Path archivo = carpeta.getRoot().toPath().resolve("sub/historial.csv");
        assertEquals(1, ArchivoCsv.escribir(archivo, List.of(r)));

        List<String[]> filas = ArchivoCsv.leer(archivo);
        assertEquals(1, filas.size());
        assertArrayEquals(r.valores(), filas.get(0));
    }

    @Test(expected = ArchivoException.class)
    public void up24_leerArchivoInexistente_lanzaArchivoException() throws ArchivoException {
        ArchivoCsv.leer(carpeta.getRoot().toPath().resolve("no-existe.csv"));
    }

    @Test
    public void up25_separarLinea_respetaCamposVaciosYComillasDobles() {
        assertArrayEquals(new String[] {"a", "", "b;c", "d\"e"}, ArchivoCsv.separarLinea("a;;\"b;c\";\"d\"\"e\""));
    }
}
