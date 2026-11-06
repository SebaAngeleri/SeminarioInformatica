package com.vigilplus.sigma.vista;

import com.vigilplus.sigma.controlador.*;
import com.vigilplus.sigma.excepcion.PersistenciaException;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.*;
import com.vigilplus.sigma.reporte.ConteoComplejo;
import com.vigilplus.sigma.reporte.ResumenAlertas;

import java.io.Console;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola del prototipo (capa de presentacion del patron MVC).
 * Solo interactua con los controladores; no conoce la base de datos.
 * Es el unico lugar donde se muestran los errores al usuario.
 */
public class VistaConsola {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int MAX_INTENTOS_LOGIN = 3;

    /** Opciones del menu. El indice del arreglo es el numero que ingresa el usuario. */
    private static final String[] OPCIONES = {
            "Salir",
            "Simular lectura de credencial (Autorizar acceso)",
            "Registrar visitante (acceso temporal)",
            "Consultar historial de accesos",
            "Ver alertas pendientes",
            "Resolver alerta",
            "Estado de los puntos de acceso",
            "Simular sensor de puerta",
            "Ver visitas vigentes",
            "Resumen de alertas por severidad y complejo",
            "Exportar historial a archivo CSV",
            "Padron local: exportar o verificar sin conexion",
            "Suspender / dar de baja credencial"
    };
    /** Opciones que solo ve y puede ejecutar un administrador. */
    private static final int[] SOLO_ADMINISTRADOR = {12};

    private final Scanner in = new Scanner(System.in);
    private final ControladorAcceso acceso = new ControladorAcceso();
    private final ControladorVisitas visitas = new ControladorVisitas();
    private final ControladorCredenciales credenciales = new ControladorCredenciales();
    private final ControladorAlertas alertas = new ControladorAlertas();
    private final ControladorConsultas consultas = new ControladorConsultas();
    private final ControladorReportes reportes = new ControladorReportes();
    private Usuario usuario;
    private PadronLocal padron;

    public void iniciar() {
        System.out.println("=== SIGMA - VigilPlus S.A. ===");
        try {
            if (!login()) return;
        } catch (SigmaException e) {
            System.err.println("No se pudo iniciar sesion: " + e.getMessage());
            return;
        }
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opcion: ");
            try {
                ejecutar(opcion);
            } catch (PersistenciaException e) {
                System.out.println("  ! " + e.getMessage());
                if (e.esFatal()) {
                    System.out.println("  El sistema no puede continuar sin la base de datos.");
                    return;
                }
            } catch (SigmaException e) {
                System.out.println("  ! " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private boolean login() throws SigmaException {
        for (int intento = 1; intento <= MAX_INTENTOS_LOGIN; intento++) {
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
        for (int i = 1; i < OPCIONES.length; i++) {
            if (habilitada(i)) System.out.printf("%2d) %s%n", i, OPCIONES[i]);
        }
        System.out.printf("%2d) %s%n", 0, OPCIONES[0]);
    }

    private boolean habilitada(int opcion) {
        for (int restringida : SOLO_ADMINISTRADOR) {
            if (restringida == opcion) return usuario.esAdministrador();
        }
        return true;
    }

    private void ejecutar(int opcion) throws SigmaException {
        if (opcion < 0 || opcion >= OPCIONES.length || !habilitada(opcion)) {
            System.out.println("Opcion invalida.");
            return;
        }
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
                LocalDate desde = leerFecha("Desde (AAAA-MM-DD): ");
                LocalDate hasta = leerFecha("Hasta (AAAA-MM-DD): ");
                List<RegistroHistorial> filas = consultas.historial(dni.isBlank() ? null : dni, null,
                        desde.atStartOfDay(), hasta.atTime(23, 59, 59));
                filas.forEach(f -> System.out.printf("  %s | %-22s | %-24s | %-7s | %s%n",
                        f.fechaHora().format(FMT), f.persona(), f.puntoAcceso(), f.sentido(), f.resultado()));
                System.out.println("  " + filas.size() + " registro(s)");
            }
            case 4 -> {
                List<Alerta> lista = alertas.pendientes();
                lista.forEach(a -> System.out.printf("  #%d %s [%s] %s - %s / %s%n", a.idAlerta(),
                        a.fechaHora().format(FMT), a.severidad(), a.tipo(), a.complejo(), a.puntoAcceso()));
                if (lista.isEmpty()) System.out.println("  No hay alertas pendientes.");
            }
            case 5 -> System.out.println(alertas.resolver(usuario, leerEntero("Id alerta: "), leerTexto("Observaciones: "))
                    ? "  -> Alerta resuelta" : "  -> No se encontro una alerta pendiente con ese id");
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
                List<Visita> lista = consultas.visitasVigentes();
                lista.forEach(v -> System.out.printf("  %-18s | %-20s | anfitrion: %-12s | %s hasta %s%n",
                        v.visitante(), v.complejo(), v.anfitrion(), v.codigoQr(), v.venceEl().format(FMT)));
                System.out.println("  " + lista.size() + " visita(s) vigente(s)");
            }
            case 9 -> mostrarResumen(reportes.resumenAlertas());
            case 10 -> {
                Path archivo = Path.of(leerTexto("Archivo destino (p. ej. historial.csv): "));
                int n = reportes.exportarHistorial(archivo, null,
                        leerFecha("Desde (AAAA-MM-DD): ").atStartOfDay(),
                        leerFecha("Hasta (AAAA-MM-DD): ").atTime(23, 59, 59));
                System.out.println("  -> " + n + " registro(s) exportados a " + archivo.toAbsolutePath());
            }
            case 11 -> menuPadron();
            case 12 -> {
                String codigo = leerTexto("Codigo de credencial: ");
                boolean ok = leerTexto("S = suspender, B = baja: ").equalsIgnoreCase("B")
                        ? credenciales.darDeBaja(usuario, codigo) : credenciales.suspender(usuario, codigo);
                System.out.println(ok ? "  -> Credencial actualizada" : "  -> Codigo inexistente");
            }
            case 0 -> System.out.println("Sesion finalizada.");
            default -> System.out.println("Opcion invalida.");
        }
    }

    private void mostrarResumen(ResumenAlertas resumen) {
        System.out.println("  Alertas pendientes: " + resumen.total());
        for (Severidad s : Severidad.values()) {
            System.out.printf("    %-6s %3d%n", s, resumen.cantidad(s));
        }
        System.out.println("  Complejos con mas alertas:");
        ConteoComplejo[] ranking = resumen.ranking();
        for (int i = 0; i < ranking.length; i++) {
            System.out.printf("    %d. %-22s %3d%n", i + 1, ranking[i].getComplejo(), ranking[i].getCantidad());
        }
    }

    private void menuPadron() throws SigmaException {
        String accion = leerTexto("E = exportar padron de un complejo, V = verificar con un padron guardado: ");
        if (accion.equalsIgnoreCase("E")) {
            int idComplejo = leerEntero("Id complejo: ");
            Path archivo = Path.of(leerTexto("Archivo destino (p. ej. padron.csv): "));
            int n = reportes.exportarPadron(archivo, idComplejo);
            System.out.println("  -> Padron con " + n + " credencial(es) guardado en " + archivo.toAbsolutePath());
        } else if (accion.equalsIgnoreCase("V")) {
            padron = reportes.cargarPadron(Path.of(leerTexto("Archivo de padron: ")));
            System.out.println("  -> Padron cargado: " + padron.tamanio() + " credencial(es). Modo SIN CONEXION.");
            String codigo;
            while (!(codigo = leerTexto("Codigo leido (vacio = terminar): ")).isEmpty()) {
                MotivoRechazo motivo = padron.verificar(codigo, leerEntero("Id punto de acceso: "), LocalDateTime.now());
                System.out.println("  -> " + (motivo == null ? "AUTORIZADO (offline)" : "RECHAZADO - " + motivo.getDescripcion()));
            }
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    private String leerTexto(String etiqueta) {
        System.out.print(etiqueta);
        if (!in.hasNextLine()) {             // fin de la entrada (Ctrl+D o archivo de prueba agotado)
            System.out.println();
            return "0";
        }
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

    private LocalDate leerFecha(String etiqueta) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(etiqueta));
            } catch (DateTimeParseException e) {
                System.out.println("Fecha invalida. Use el formato AAAA-MM-DD, por ejemplo 2026-09-21.");
            }
        }
    }
}
