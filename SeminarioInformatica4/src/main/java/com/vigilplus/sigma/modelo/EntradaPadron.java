package com.vigilplus.sigma.modelo;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Credencial tal como se guarda en el padron local de un controlador.
 * Incluye los puntos habilitados como arreglo de enteros, porque el padron
 * se usa para validar accesos sin conexion con el servidor central.
 */
public record EntradaPadron(String codigo, String titular, Credencial.Tipo tipo, Credencial.Estado estado,
                            LocalDateTime vigenciaDesde, LocalDateTime vigenciaHasta, int[] puntos)
        implements Exportable {

    private static final String[] ENCABEZADOS =
            {"codigo", "titular", "tipo", "estado", "vigencia_desde", "vigencia_hasta", "puntos"};
    private static final String SEPARADOR_PUNTOS = "|";

    public EntradaPadron {
        puntos = puntos.clone();      // copia defensiva: el record no expone su arreglo interno
        Arrays.sort(puntos);
    }

    @Override
    public int[] puntos() {
        return puntos.clone();
    }

    /** Indica si la credencial esta habilitada en el punto (busqueda binaria sobre el arreglo ordenado). */
    public boolean habilitadaEn(int idPunto) {
        return Arrays.binarySearch(puntos, idPunto) >= 0;
    }

    /** Convierte la entrada en una Credencial para reutilizar las mismas reglas de acceso. */
    public Credencial comoCredencial() {
        return new Credencial(0, 0, codigo, tipo, vigenciaDesde, vigenciaHasta, estado, false);
    }

    @Override
    public String[] encabezados() {
        return ENCABEZADOS.clone();
    }

    @Override
    public String[] valores() {
        String[] ids = new String[puntos.length];
        for (int i = 0; i < puntos.length; i++) {
            ids[i] = String.valueOf(puntos[i]);
        }
        return new String[] {codigo, titular, tipo.name(), estado.name(), vigenciaDesde.toString(),
                vigenciaHasta.toString(), String.join(SEPARADOR_PUNTOS, ids)};
    }

    /**
     * Reconstruye una entrada a partir de los campos leidos de una linea del archivo.
     * @param numeroLinea se informa en el mensaje de error para ubicar el problema
     */
    public static EntradaPadron desdeCampos(String[] campos, int numeroLinea) throws DatoInvalidoException {
        if (campos.length != ENCABEZADOS.length) {
            throw new DatoInvalidoException("Linea " + numeroLinea + ": se esperaban " + ENCABEZADOS.length
                    + " columnas y hay " + campos.length);
        }
        try {
            String[] ids = campos[6].isBlank() ? new String[0] : campos[6].split("\\|");
            int[] puntos = new int[ids.length];
            for (int i = 0; i < ids.length; i++) {
                puntos[i] = Integer.parseInt(ids[i].trim());
            }
            return new EntradaPadron(campos[0], campos[1], Credencial.Tipo.valueOf(campos[2]),
                    Credencial.Estado.valueOf(campos[3]), LocalDateTime.parse(campos[4]),
                    LocalDateTime.parse(campos[5]), puntos);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new DatoInvalidoException("Linea " + numeroLinea + ": numero o fecha con formato invalido ("
                    + e.getMessage() + ")");
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("Linea " + numeroLinea + ": tipo o estado desconocido ("
                    + e.getMessage() + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EntradaPadron e && codigo.equals(e.codigo) && titular.equals(e.titular)
                && tipo == e.tipo && estado == e.estado && vigenciaDesde.equals(e.vigenciaDesde)
                && vigenciaHasta.equals(e.vigenciaHasta) && Arrays.equals(puntos, e.puntos);
    }

    @Override
    public int hashCode() {
        return 31 * codigo.hashCode() + Arrays.hashCode(puntos);
    }

    @Override
    public String toString() {
        return codigo + " (" + titular + ", " + estado + ", puntos " + Arrays.toString(puntos) + ")";
    }
}
