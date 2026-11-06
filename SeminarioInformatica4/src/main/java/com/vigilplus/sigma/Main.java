package com.vigilplus.sigma;

import com.vigilplus.sigma.controlador.ControladorAcceso;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.EventoAcceso;
import com.vigilplus.sigma.modelo.ResultadoAcceso;
import com.vigilplus.sigma.vista.VistaConsola;

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
            } catch (NumberFormatException e) {
                System.err.println("El id del punto de acceso debe ser numerico: " + args[1]);
                System.exit(2);
            } catch (SigmaException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } else {
            new VistaConsola().iniciar();
        }
    }
}
