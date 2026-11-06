package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.excepcion.PersistenciaException;
import com.vigilplus.sigma.excepcion.SigmaException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Administra la conexion y la transaccion de cada operacion.
 * Es el limite entre la capa de persistencia y el resto del sistema: toda
 * SQLException que ocurre adentro se traduce a PersistenciaException, de modo
 * que los controladores y la vista nunca manejan excepciones de JDBC.
 */
public final class Transaccion {

    /** Bloque de operaciones que usa una conexion abierta. */
    @FunctionalInterface
    public interface Bloque<T> {
        T ejecutar(Connection con) throws SQLException, SigmaException;
    }

    private Transaccion() { }

    /**
     * Ejecuta el bloque como una unica transaccion: commit si todo sale bien
     * y rollback ante cualquier error, ya sea de la base de datos o de una
     * regla de negocio. La conexion se cierra siempre (try-with-resources).
     */
    public static <T> T ejecutar(Bloque<T> bloque) throws SigmaException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                T resultado = bloque.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException | SigmaException | RuntimeException e) {
                deshacer(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw PersistenciaException.desde(e);
        }
    }

    /** Ejecuta un bloque de solo lectura (sin transaccion explicita). */
    public static <T> T consultar(Bloque<T> bloque) throws SigmaException {
        try (Connection con = Conexion.obtener()) {
            con.setReadOnly(true);
            return bloque.ejecutar(con);
        } catch (SQLException e) {
            throw PersistenciaException.desde(e);
        }
    }

    /** Revierte la transaccion; si el rollback falla, se adjunta al error original sin ocultarlo. */
    private static void deshacer(Connection con, Exception original) {
        try {
            con.rollback();
        } catch (SQLException errorRollback) {
            original.addSuppressed(errorRollback);
        }
    }
}
