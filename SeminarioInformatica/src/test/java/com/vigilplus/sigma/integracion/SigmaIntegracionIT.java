package com.vigilplus.sigma.integracion;

import com.vigilplus.sigma.controlador.*;
import com.vigilplus.sigma.modelo.*;
import com.vigilplus.sigma.persistencia.Conexion;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Pruebas de integracion (controlador + DAO + MySQL). Requieren la base "sigma"
 * creada con los scripts 01 y 02. Crean sus propios datos (prefijo IT-) y los
 * eliminan al finalizar, por lo que pueden ejecutarse repetidas veces.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SigmaIntegracionIT {

    private static final String DNI = "IT000001";
    private static final String DNI_VISITA = "IT000002";
    private static long idSensorInicial;

    private final ControladorAcceso acceso = new ControladorAcceso();
    private final ControladorVisitas visitas = new ControladorVisitas();
    private final ControladorAlertas alertas = new ControladorAlertas();
    private final ControladorConsultas consultas = new ControladorConsultas();
    private final ControladorCredenciales credenciales = new ControladorCredenciales();

    @BeforeClass
    public static void prepararDatos() throws SQLException {
        try (Connection con = Conexion.obtener()) {
            limpiar(con);
            idSensorInicial = unLong(con, "SELECT COALESCE(MAX(id_evento_sensor), 0) FROM evento_sensor");
            ejecutar(con, "INSERT INTO persona (dni, nombre, apellido, id_rol) VALUES ('" + DNI + "', 'Prueba', 'Integracion', 2)");
            ejecutar(con, "INSERT INTO credencial (id_persona, codigo, tipo, vigencia_desde, vigencia_hasta, estado) "
                    + "SELECT id_persona, 'IT-OK', 'RFID', NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 1 DAY, 'ACTIVA' FROM persona WHERE dni='" + DNI + "' UNION ALL "
                    + "SELECT id_persona, 'IT-VENC', 'RFID', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY, 'ACTIVA' FROM persona WHERE dni='" + DNI + "' UNION ALL "
                    + "SELECT id_persona, 'IT-SUSP', 'RFID', NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 1 DAY, 'SUSPENDIDA' FROM persona WHERE dni='" + DNI + "'");
            ejecutar(con, "INSERT INTO credencial_punto_acceso SELECT id_credencial, p.id FROM credencial "
                    + "CROSS JOIN (SELECT 1 AS id UNION SELECT 2) p WHERE codigo LIKE 'IT-%'");
        }
    }

    @AfterClass
    public static void limpiarDatos() throws SQLException {
        try (Connection con = Conexion.obtener()) {
            limpiar(con);
        }
    }

    @Test
    public void cp01_credencialValida_seAutorizaYRegistraEvento() throws SQLException {
        ResultadoAcceso r = acceso.autorizarAcceso("IT-OK", 1, EventoAcceso.Sentido.INGRESO);
        assertTrue(r.autorizado());
        assertEquals("AUTORIZADO", texto("SELECT resultado FROM evento_acceso WHERE id_evento = " + r.idEvento()));
        assertEquals(0, unLong("SELECT COUNT(*) FROM alerta WHERE id_evento = " + r.idEvento()));
    }

    @Test
    public void cp02_credencialVencida_seRechazaYGeneraAlerta() throws SQLException {
        ResultadoAcceso r = acceso.autorizarAcceso("IT-VENC", 1, EventoAcceso.Sentido.INGRESO);
        assertEquals(MotivoRechazo.CREDENCIAL_VENCIDA, r.motivo());
        assertEquals("CRED_VENCIDA", tipoAlertaDeEvento(r.idEvento()));
    }

    @Test
    public void cp03_credencialSuspendida_seRechaza() throws SQLException {
        ResultadoAcceso r = acceso.autorizarAcceso("IT-SUSP", 1, EventoAcceso.Sentido.INGRESO);
        assertEquals(MotivoRechazo.CREDENCIAL_NO_ACTIVA, r.motivo());
        assertEquals("CRED_NO_AUTORIZADA", tipoAlertaDeEvento(r.idEvento()));
    }

    @Test
    public void cp04_puntoNoHabilitado_seRechaza() throws SQLException {
        ResultadoAcceso r = acceso.autorizarAcceso("IT-OK", 3, EventoAcceso.Sentido.INGRESO);
        assertEquals(MotivoRechazo.PUNTO_NO_HABILITADO, r.motivo());
    }

    @Test
    public void cp05_codigoInexistente_registraEventoYAlertaAlta() throws SQLException {
        ResultadoAcceso r = acceso.autorizarAcceso("IT-FALSO", 1, EventoAcceso.Sentido.INGRESO);
        assertFalse(r.autorizado());
        assertEquals("IT-FALSO", texto("SELECT codigo_no_reconocido FROM evento_acceso WHERE id_evento = " + r.idEvento()));
        assertEquals("CRED_INEXISTENTE", tipoAlertaDeEvento(r.idEvento()));
    }

    @Test
    public void cp06_registroDeEventoEnMenosDeDosSegundos_RNF07() throws SQLException {
        long inicio = System.nanoTime();
        acceso.autorizarAcceso("IT-OK", 1, EventoAcceso.Sentido.EGRESO);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue("Demoro " + ms + " ms", ms < 2000);
    }

    @Test
    public void cp07_visitante_obtieneQrTemporalValidoSoloEnSuComplejo() throws SQLException {
        Usuario operador = consultas.iniciarSesion("mfernandez", "Operador#2026");
        Credencial qr = visitas.registrarVisita(operador, DNI_VISITA, "Visita", "Prueba", "11-0000-0000",
                2, "Diego Paz", "Entrega", 2);
        assertTrue(qr.isTemporal());
        assertTrue(acceso.autorizarAcceso(qr.getCodigo(), 3, EventoAcceso.Sentido.INGRESO).autorizado());
        assertEquals(MotivoRechazo.PUNTO_NO_HABILITADO,
                acceso.autorizarAcceso(qr.getCodigo(), 1, EventoAcceso.Sentido.INGRESO).motivo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cp08_visitaConDuracionInvalida_seRechaza() throws SQLException {
        Usuario operador = consultas.iniciarSesion("mfernandez", "Operador#2026");
        visitas.registrarVisita(operador, DNI_VISITA, "V", "P", null, 2, "X", "Y", 48);
    }

    @Test
    public void cp09_aperturaSinAccesoAutorizado_generaPuertaForzada() throws SQLException {
        assertTrue(alertas.procesarLecturaSensor(4, "ABIERTA"));
        alertas.procesarLecturaSensor(4, "CERRADA");
    }

    @Test
    public void cp10_aperturaLuegoDeAccesoAutorizado_noGeneraAlerta() throws SQLException {
        assertTrue(acceso.autorizarAcceso("IT-OK", 2, EventoAcceso.Sentido.INGRESO).autorizado());
        assertFalse(alertas.procesarLecturaSensor(2, "ABIERTA"));
        alertas.procesarLecturaSensor(2, "CERRADA");
    }

    @Test
    public void cp11_puertaAbiertaExcedida_generaUnaSolaAlerta() throws SQLException {
        ejecutarSql("INSERT INTO evento_sensor (id_sensor, fecha_hora, estado_puerta) "
                  + "VALUES (3, NOW() - INTERVAL 5 MINUTE, 'ABIERTA')");
        assertEquals(1, alertas.verificarPuertasAbiertas());
        assertEquals(0, alertas.verificarPuertasAbiertas());   // idempotente
        ejecutarSql("INSERT INTO evento_sensor (id_sensor, estado_puerta) VALUES (3, 'CERRADA')");
    }

    @Test
    public void cp12_historialFiltraPorPersonaYFechas() throws SQLException {
        List<RegistroHistorial> h = consultas.historial(DNI, null,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusMinutes(1));
        assertTrue(h.size() >= 5);
        assertTrue(h.stream().allMatch(f -> f.persona().equals("Integracion, Prueba")));
    }

    @Test
    public void cp13_loginIncorrecto_devuelveNull() throws SQLException {
        assertNull(consultas.iniciarSesion("mfernandez", "clave-erronea"));
        assertNotNull(consultas.iniciarSesion("lgimenez", "Admin#2026"));
    }

    @Test(expected = SecurityException.class)
    public void cp14_operadorNoPuedeDarDeBajaCredenciales() throws SQLException {
        Usuario operador = consultas.iniciarSesion("mfernandez", "Operador#2026");
        credenciales.darDeBaja(operador, "IT-OK");
    }

    @Test
    public void cp15_bajaLogica_impideAccesosPosteriores() throws SQLException {
        Usuario admin = consultas.iniciarSesion("lgimenez", "Admin#2026");
        assertTrue(credenciales.darDeBaja(admin, "IT-SUSP"));
        assertEquals(MotivoRechazo.CREDENCIAL_NO_ACTIVA,
                acceso.autorizarAcceso("IT-SUSP", 1, EventoAcceso.Sentido.INGRESO).motivo());
    }

    // ---------------------------------------------------------------- utilidades

    private static void limpiar(Connection con) throws SQLException {
        String credIT = "SELECT c.id_credencial FROM credencial c JOIN persona p ON p.id_persona = c.id_persona WHERE p.dni LIKE 'IT%'";
        String evIT = "SELECT id_evento FROM evento_acceso WHERE codigo_no_reconocido LIKE 'IT-%' OR id_credencial IN (" + credIT + ")";
        ejecutar(con, "DELETE FROM alerta WHERE id_evento IN (SELECT id_evento FROM (" + evIT + ") x)");
        ejecutar(con, "DELETE FROM evento_acceso WHERE id_evento IN (SELECT id_evento FROM (" + evIT + ") x)");
        if (idSensorInicial > 0) {
            ejecutar(con, "DELETE FROM alerta WHERE id_evento_sensor > " + idSensorInicial);
            ejecutar(con, "DELETE FROM evento_sensor WHERE id_evento_sensor > " + idSensorInicial);
        }
        ejecutar(con, "DELETE FROM visita WHERE id_persona IN (SELECT id_persona FROM persona WHERE dni LIKE 'IT%')");
        ejecutar(con, "DELETE FROM credencial_punto_acceso WHERE id_credencial IN (SELECT id_credencial FROM (" + credIT + ") x)");
        ejecutar(con, "DELETE FROM credencial WHERE id_credencial IN (SELECT id_credencial FROM (" + credIT + ") x)");
        ejecutar(con, "DELETE FROM persona WHERE dni LIKE 'IT%'");
    }

    private String tipoAlertaDeEvento(long idEvento) throws SQLException {
        return texto("SELECT t.codigo FROM alerta a JOIN tipo_alerta t ON t.id_tipo_alerta = a.id_tipo_alerta "
                   + "WHERE a.id_evento = " + idEvento);
    }

    private static void ejecutar(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void ejecutarSql(String sql) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            ejecutar(con, sql);
        }
    }

    private static long unLong(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static long unLong(String sql) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            return unLong(con, sql);
        }
    }

    private static String texto(String sql) throws SQLException {
        try (Connection con = Conexion.obtener(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }
}
