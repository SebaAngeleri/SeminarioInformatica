package com.vigilplus.sigma;

import com.vigilplus.sigma.controlador.ControladorAcceso;
import com.vigilplus.sigma.modelo.EventoAcceso;

import java.sql.SQLException;

/**
 * Punto de entrada del prototipo SIGMA.
 * Simula la lectura de una credencial en un punto de acceso
 * y muestra el resultado de la verificación.
 *
 * Requiere que la base de datos "sigma" (ver sql/sigma_schema.sql)
 * esté disponible en MySQL y accesible con los datos configurados
 * en com.vigilplus.sigma.persistencia.Conexion.
 */
public class Main {

    public static void main(String[] args) {
        ControladorAcceso controlador = new ControladorAcceso();

        String codigoCredencial = args.length > 0 ? args[0] : "RFID-0001";
        int idPuntoAcceso = args.length > 1 ? Integer.parseInt(args[1]) : 1;

        try {
            EventoAcceso.Resultado resultado = controlador.autorizarAcceso(
                    codigoCredencial, idPuntoAcceso, EventoAcceso.Sentido.INGRESO);

            System.out.println("Credencial: " + codigoCredencial);
            System.out.println("Punto de acceso: " + idPuntoAcceso);
            System.out.println("Resultado: " + resultado);

        } catch (SQLException e) {
            System.err.println("Error al conectar o consultar la base de datos: " + e.getMessage());
        }
    }
}
