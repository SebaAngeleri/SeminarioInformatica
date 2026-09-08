package com.vigilplus.sigma.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilidad de conexión a la base de datos MySQL del sistema SIGMA.
 * Centraliza los datos de conexión para que el resto de las clases
 * de persistencia (DAO) no dependan de la configuración concreta.
 */
public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/sigma?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "sigma_app";
    private static final String CLAVE = "cambiar_en_produccion";

    private Conexion() {
        // Clase utilitaria: no debe instanciarse.
    }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
