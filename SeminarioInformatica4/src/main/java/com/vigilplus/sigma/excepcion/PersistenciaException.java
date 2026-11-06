package com.vigilplus.sigma.excepcion;

import java.sql.SQLException;

/**
 * Error en la interaccion con la base de datos MySQL.
 * Envuelve la SQLException original (que se conserva como causa para el
 * diagnostico) y la traduce a un mensaje comprensible para el operador,
 * segun el SQLState y el codigo de error de MySQL.
 */
public class PersistenciaException extends SigmaException {

    private static final long serialVersionUID = 1L;

    /** Categoria del error, util para decidir como reaccionar en la vista. */
    public enum Tipo { CONEXION, CREDENCIALES_BD, DUPLICADO, INTEGRIDAD, BLOQUEO, OTRO }

    private final Tipo tipo;

    private PersistenciaException(Tipo tipo, String mensaje, SQLException causa) {
        super(mensaje, causa);
        this.tipo = tipo;
    }

    /** Traduce una SQLException de JDBC a una PersistenciaException. */
    public static PersistenciaException desde(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        int codigo = e.getErrorCode();

        if (estado.startsWith("08")) {
            return new PersistenciaException(Tipo.CONEXION,
                    "No se pudo conectar con la base de datos. Verifique que el servidor MySQL este en linea.", e);
        }
        if (estado.startsWith("28") || codigo == 1045) {
            return new PersistenciaException(Tipo.CREDENCIALES_BD,
                    "La aplicacion no tiene permiso para acceder a la base de datos (usuario o clave de MySQL).", e);
        }
        if (codigo == 1062) {
            return new PersistenciaException(Tipo.DUPLICADO,
                    "Ya existe un registro con ese dato (DNI, codigo o usuario duplicado).", e);
        }
        if (codigo == 1451 || codigo == 1452 || codigo == 3819 || estado.startsWith("23")) {
            return new PersistenciaException(Tipo.INTEGRIDAD,
                    "La operacion viola una regla de integridad de la base de datos.", e);
        }
        if (codigo == 1205 || codigo == 1213) {
            return new PersistenciaException(Tipo.BLOQUEO,
                    "La base de datos esta ocupada. Reintente la operacion.", e);
        }
        return new PersistenciaException(Tipo.OTRO, "Error de base de datos: " + e.getMessage(), e);
    }

    public Tipo getTipo() { return tipo; }

    /** Los errores de conexion o de permisos impiden seguir trabajando. */
    public boolean esFatal() {
        return tipo == Tipo.CONEXION || tipo == Tipo.CREDENCIALES_BD;
    }
}
