package com.vigilplus.sigma.util;

import com.vigilplus.sigma.excepcion.ArchivoException;
import com.vigilplus.sigma.modelo.Exportable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura y escritura de archivos CSV separados por punto y coma (formato que
 * abre directamente Excel con configuracion regional de Argentina).
 * Los campos que contienen el separador, comillas o saltos de linea se
 * encierran entre comillas dobles.
 */
public final class ArchivoCsv {

    public static final char SEPARADOR = ';';

    private ArchivoCsv() { }

    /**
     * Escribe una fila de encabezados y una fila por cada objeto.
     * @return cantidad de filas de datos escritas
     */
    public static int escribir(Path ruta, List<? extends Exportable> filas) throws ArchivoException {
        if (filas.isEmpty()) throw new IllegalArgumentException("No hay filas para escribir");
        try {
            Path carpeta = ruta.toAbsolutePath().getParent();
            if (carpeta != null) Files.createDirectories(carpeta);
            try (BufferedWriter out = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
                out.write(unirLinea(filas.get(0).encabezados()));
                out.newLine();
                for (Exportable fila : filas) {
                    out.write(unirLinea(fila.valores()));
                    out.newLine();
                }
            }
            return filas.size();
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir el archivo " + ruta + ": " + e.getMessage(), e);
        }
    }

    /**
     * Lee el archivo y devuelve cada linea de datos (sin el encabezado) como un arreglo de campos.
     * Las lineas vacias se ignoran.
     */
    public static List<String[]> leer(Path ruta) throws ArchivoException {
        List<String[]> filas = new ArrayList<>();
        try (BufferedReader in = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea = in.readLine();        // encabezado
            if (linea == null) return filas;
            while ((linea = in.readLine()) != null) {
                if (!linea.isBlank()) filas.add(separarLinea(linea));
            }
            return filas;
        } catch (NoSuchFileException e) {
            throw new ArchivoException("No existe el archivo " + ruta, e);
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer el archivo " + ruta + ": " + e.getMessage(), e);
        }
    }

    static String unirLinea(String[] campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(SEPARADOR);
            sb.append(escapar(campos[i] == null ? "" : campos[i]));
        }
        return sb.toString();
    }

    private static String escapar(String campo) {
        boolean requiereComillas = campo.indexOf(SEPARADOR) >= 0 || campo.indexOf('"') >= 0
                || campo.indexOf('\n') >= 0;
        return requiereComillas ? '"' + campo.replace("\"", "\"\"") + '"' : campo;
    }

    /** Separa una linea en campos, respetando los campos entre comillas. */
    static String[] separarLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"' && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else if (c == '"') {
                    entreComillas = false;
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }
}
