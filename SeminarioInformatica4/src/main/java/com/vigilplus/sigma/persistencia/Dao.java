package com.vigilplus.sigma.persistencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato del patron DAO (Data Access Object) para las entidades de SIGMA.
 * Los controladores dependen de esta abstraccion y no del SQL concreto, por lo
 * que la forma de almacenar los datos puede cambiar sin modificar la logica
 * de negocio.
 *
 * @param <T> tipo de objeto del modelo que administra el DAO
 * @param <K> tipo de su identificador
 */
public interface Dao<T, K> {

    /** Devuelve el objeto con ese identificador, o null si no existe. */
    T buscarPorId(Connection con, K id) throws SQLException;

    /** Devuelve todos los objetos, en el orden natural de la consulta. */
    List<T> listarTodos(Connection con) throws SQLException;
}
