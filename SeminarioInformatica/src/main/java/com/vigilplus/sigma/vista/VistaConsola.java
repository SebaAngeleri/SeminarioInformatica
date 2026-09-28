package com.vigilplus.sigma.vista;

import com.vigilplus.sigma.controlador.*;
import com.vigilplus.sigma.modelo.*;

import java.io.Console;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola del prototipo (capa de presentacion del patron MVC).
 * Solo interactua con los controladores; no conoce la base de datos.
 */
public class VistaConsola {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Scanner in = new Scanner(System.in);
    private final ControladorAcceso acceso = new ControladorAcceso();
    private final ControladorVisitas visitas = new ControladorVisitas();
    private final ControladorCredenciales credenciales = new ControladorCredenciales();
    private final ControladorAlertas alertas = new ControladorAlertas();
    private final ControladorConsultas consultas = new ControladorConsultas();
    private Usuario usuario;

    public void iniciar() {
        System.out.println("=== SIGMA - VigilPlus S.A. ===");
        try {
            if (!login()) return;
            int opcion;
            do {
                mostrarMenu();
                opcion = leerEntero("Opcion: ");
                try {
                    ejecutar(opcion);
                } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                    System.out.println("  ! " + e.getMessage());
                }
            } while (opcion != 0);
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        }
    }

    private boolean login() throws SQLException {
        for (int intento = 1; intento <= 3; intento++) {
            String user = leerTexto("Usuario: ");
            Console consola = System.console();
            String pass = consola != null ? new String(consola.readPassword("Clave: ")) : leerTexto("Clave: ");
            usuario = consultas.iniciarSesion(user, pass);
            if (usuario != null) {
                System.out.println("Bienvenido/a " + usuario.username() + " (" + usuario.perfil() + ")");
                return true;
            }
            System.out.println("Usuario o clave incorrectos.");
        }
        return false;
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("1) Simular lectura de credencial (Autorizar acceso)");
        System.out.println("2) Registrar visitante (acceso temporal)");
        System.out.println("3) Consultar historial de accesos");
        System.out.println("4) Ver alertas pendientes");
        System.out.println("5) Resolver alerta");
        System.out.println("6) Estado de los puntos de acceso");
        System.out.println("7) Simular sensor de puerta");
        if (usuario.esAdministrador()) System.out.println("8) Suspender / dar de baja credencial");
        System.out.println("0) Salir");
    }

    private void ejecutar(int opcion) throws SQLException {
        switch (opcion) {
            case 1 -> {
                ResultadoAcceso r = acceso.autorizarAcceso(leerTexto("Codigo leido: "),
                        leerEntero("Id punto de acceso: "), EventoAcceso.Sentido.INGRESO);
                System.out.println("  -> " + r.descripcion() + " (evento #" + r.idEvento() + ")");
            }
            case 2 -> {
                Credencial qr = visitas.registrarVisita(usuario, leerTexto("DNI: "), leerTexto("Nombre: "),
                        leerTexto("Apellido: "), leerTexto("Telefono: "), leerEntero("Id complejo: "),
                        leerTexto("Anfitrion: "), leerTexto("Motivo: "), leerEntero("Duracion (horas): "));
                System.out.println("  -> Credencial QR emitida: " + qr.getCodigo()
                        + " valida hasta " + qr.getVigenciaHasta().format(FMT));
            }
            case 3 -> {
                String dni = leerTexto("DNI (vacio = todos): ");
                LocalDate desde = LocalDate.parse(leerTexto("Desde (AAAA-MM-DD): "));
                LocalDate hasta = LocalDate.parse(leerTexto("Hasta (AAAA-MM-DD): "));
                List<RegistroHistorial> filas = consultas.historial(dni.isBlank() ? null : dni, null,
                        desde.atStartOfDay(), hasta.atTime(23, 59, 59));
                filas.forEach(f -> System.out.printf("  %s | %-20s | %-20s | %-7s | %s%n",
                        f.fechaHora().format(FMT), f.persona(), f.puntoAcceso(), f.sentido(), f.resultado()));
                System.out.println("  " + filas.size() + " registro(s)");
            }
            case 4 -> alertas.pendientes().forEach(a -> System.out.printf("  #%d %s [%s] %s - %s / %s%n",
                    a.idAlerta(), a.fechaHora().format(FMT), a.severidad(), a.tipo(), a.complejo(), a.puntoAcceso()));
            case 5 -> System.out.println(alertas.resolver(usuario, leerEntero("Id alerta: "),
                    leerTexto("Observaciones: ")) ? "  -> Alerta resuelta" : "  -> No se encontro una alerta pendiente con ese id");
            case 6 -> consultas.estadoPuntos().forEach(p -> System.out.printf("  %-20s | %-25s | %-8s | %s%n",
                    p.complejo(), p.identificador(), p.estado(), p.enLinea() ? "EN LINEA" : "FUERA DE LINEA"));
            case 7 -> {
                boolean alerta = alertas.procesarLecturaSensor(leerEntero("Id sensor: "),
                        leerTexto("Estado (ABIERTA/CERRADA): ").toUpperCase());
                System.out.println(alerta ? "  -> ALERTA: apertura sin acceso autorizado" : "  -> Lectura registrada");
                int nuevas = alertas.verificarPuertasAbiertas();
                if (nuevas > 0) System.out.println("  -> " + nuevas + " alerta(s) de puerta abierta");
            }
            case 8 -> {
                String codigo = leerTexto("Codigo de credencial: ");
                boolean ok = leerTexto("S = suspender, B = baja: ").equalsIgnoreCase("B")
                        ? credenciales.darDeBaja(usuario, codigo) : credenciales.suspender(usuario, codigo);
                System.out.println(ok ? "  -> Credencial actualizada" : "  -> Codigo inexistente");
            }
            case 0 -> System.out.println("Sesion finalizada.");
            default -> System.out.println("Opcion invalida.");
        }
    }

    private String leerTexto(String etiqueta) {
        System.out.print(etiqueta);
        return in.nextLine().trim();
    }

    private int leerEntero(String etiqueta) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(etiqueta));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }
}
