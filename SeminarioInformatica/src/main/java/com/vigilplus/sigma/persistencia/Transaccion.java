package com.vigilplus.sigma.persistencia;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta un bloque de operaciones como una unica transaccion
 * (commit si todo sale bien, rollback ante cualquier error).
 */
public final class Transaccion {

    @FunctionalInterface
    public interface Bloque<T> {
        T ejecutar(Connection con) throws SQLException;
    }

    private Transaccion() { }

    public static <T> T ejecutar(Bloque<T> bloque) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                T resultado = bloque.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
