package com.vigilplus.sigma.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto unico de acceso a la base de datos MySQL (JDBC).
 * Los datos de conexion se leen de propiedades del sistema o variables
 * de entorno, para no dejar credenciales fijas en el codigo:
 *   -Dsigma.db.url  / SIGMA_DB_URL
 *   -Dsigma.db.user / SIGMA_DB_USER
 *   -Dsigma.db.pass / SIGMA_DB_PASS
 * En produccion la URL debe incluir sslMode=REQUIRED (canal TLS).
 */
public final class Conexion {

    private static final String URL = config("sigma.db.url", "SIGMA_DB_URL",
            "jdbc:mysql://localhost:3306/sigma?serverTimezone=America/Argentina/Buenos_Aires");
    private static final String USUARIO = config("sigma.db.user", "SIGMA_DB_USER", "sigma_app");
    private static final String CLAVE = config("sigma.db.pass", "SIGMA_DB_PASS", "cambiar_en_produccion");

    private Conexion() { }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    private static String config(String propiedad, String variable, String porDefecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null) valor = System.getenv(variable);
        return valor != null ? valor : porDefecto;
    }
}
