package com.vigilplus.sigma;

import com.vigilplus.sigma.controlador.ControladorAcceso;
import com.vigilplus.sigma.modelo.EventoAcceso;
import com.vigilplus.sigma.modelo.ResultadoAcceso;
import com.vigilplus.sigma.vista.VistaConsola;

import java.sql.SQLException;

/**
 * Punto de entrada del prototipo SIGMA.
 *  - Sin argumentos: inicia la vista de consola interactiva (requiere login).
 *  - Con argumentos "CODIGO ID_PUNTO": simula la lectura de una credencial, tal
 *    como lo haria el controlador local de un punto de acceso.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length >= 2) {
            try {
                ResultadoAcceso r = new ControladorAcceso().autorizarAcceso(
                        args[0], Integer.parseInt(args[1]), EventoAcceso.Sentido.INGRESO);
                System.out.println("Credencial: " + args[0] + " | Punto: " + args[1] + " | " + r.descripcion());
            } catch (SQLException e) {
                System.err.println("Error de base de datos: " + e.getMessage());
            }
        } else {
            new VistaConsola().iniciar();
        }
    }
}
