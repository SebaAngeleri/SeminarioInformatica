package com.vigilplus.sigma.persistencia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta que concentra el codigo JDBC comun a todos los DAO.
 * Aplica el patron Template Method: los metodos de consulta definen el
 * algoritmo (preparar la sentencia, asignar parametros, recorrer el
 * ResultSet y cerrar los recursos) y delegan en cada subclase solo el paso
 * que cambia: como convertir una fila en un objeto del modelo (mapear).
 *
 * Los parametros se reciben como arreglo (varargs Object...), porque su
 * cantidad es fija para cada sentencia SQL, y los resultados se devuelven en
 * un ArrayList, porque la cantidad de filas no se conoce de antemano.
 *
 * @param <T> tipo de objeto que produce cada fila
 */
public abstract class DaoBase<T> {

    /** Convierte una fila de una consulta en otro tipo de objeto. */
    @FunctionalInterface
    protected interface Mapeador<R> {
        R mapear(ResultSet rs) throws SQLException;
    }

    /** Paso variable del Template Method: convierte la fila actual en un objeto T. */
    protected abstract T mapear(ResultSet rs) throws SQLException;

    /** Ejecuta una consulta que devuelve a lo sumo una fila. */
    protected T consultarUno(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, Statement.NO_GENERATED_KEYS, parametros);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapear(rs) : null;
        }
    }

    /** Ejecuta una consulta y convierte cada fila con el mapeador propio del DAO. */
    protected List<T> consultarLista(Connection con, String sql, Object... parametros) throws SQLException {
        return consultar(con, sql, this::mapear, parametros);
    }

    /** Ejecuta una consulta y convierte cada fila con un mapeador ad hoc (p. ej., una lista de ids). */
    protected <R> List<R> consultar(Connection con, String sql, Mapeador<R> mapeador, Object... parametros)
            throws SQLException {
        List<R> resultado = new ArrayList<>();
        try (PreparedStatement ps = preparar(con, sql, Statement.NO_GENERATED_KEYS, parametros);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapeador.mapear(rs));
            }
        }
        return resultado;
    }

    /** Indica si la consulta devuelve al menos una fila. */
    protected boolean existe(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, Statement.NO_GENERATED_KEYS, parametros);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    /** Ejecuta un UPDATE o DELETE y devuelve la cantidad de filas afectadas. */
    protected int actualizar(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, Statement.NO_GENERATED_KEYS, parametros)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Ejecuta un INSERT y devuelve la clave autoincremental generada.
     * @throws SQLException si no se inserto ninguna fila (p. ej., un INSERT ... SELECT sin resultados)
     */
    protected long insertar(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, Statement.RETURN_GENERATED_KEYS, parametros)) {
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No se inserto ningun registro", "02000");
            }
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("La base no devolvio la clave generada");
                return claves.getLong(1);
            }
        }
    }

    /**
     * Crea la sentencia y asigna los parametros recorriendo el arreglo.
     * Se usa siempre PreparedStatement para evitar la inyeccion SQL.
     */
    private PreparedStatement preparar(Connection con, String sql, int claves, Object[] parametros)
            throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql, claves);
        try {
            for (int i = 0; i < parametros.length; i++) {
                asignar(ps, i + 1, parametros[i]);
            }
            return ps;
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
    }

    private static void asignar(PreparedStatement ps, int posicion, Object valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicion, Types.NULL);
        } else if (valor instanceof LocalDateTime fecha) {
            ps.setTimestamp(posicion, Timestamp.valueOf(fecha));
        } else if (valor instanceof Enum<?> constante) {
            ps.setString(posicion, constante.name());
        } else {
            ps.setObject(posicion, valor);
        }
    }

    /** Lee una columna DATETIME que puede ser nula. */
    protected static LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp ts = rs.getTimestamp(columna);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
