package com.vigilplus.sigma.excepcion;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

/** Traduccion de los errores de MySQL (SQLState y codigo) a mensajes para el operador. */
public class PersistenciaExceptionTest {

    private static PersistenciaException.Tipo tipo(String sqlState, int codigo) {
        return PersistenciaException.desde(new SQLException("x", sqlState, codigo)).getTipo();
    }

    @Test
    public void up26_cadaErrorDeMySqlSeClasificaCorrectamente() {
        assertEquals(PersistenciaException.Tipo.CONEXION, tipo("08S01", 0));
        assertEquals(PersistenciaException.Tipo.CREDENCIALES_BD, tipo("28000", 1045));
        assertEquals(PersistenciaException.Tipo.DUPLICADO, tipo("23000", 1062));
        assertEquals(PersistenciaException.Tipo.INTEGRIDAD, tipo("23000", 1451));
        assertEquals(PersistenciaException.Tipo.INTEGRIDAD, tipo("HY000", 3819));
        assertEquals(PersistenciaException.Tipo.BLOQUEO, tipo("40001", 1213));
        assertEquals(PersistenciaException.Tipo.OTRO, tipo(null, 0));
    }

    @Test
    public void up27_soloLosErroresDeConexionSonFatales_yLaCausaSeConserva() {
        SQLException original = new SQLException("Communications link failure", "08S01");
        PersistenciaException e = PersistenciaException.desde(original);
        assertTrue(e.esFatal());
        assertSame(original, e.getCause());
        assertFalse(PersistenciaException.desde(new SQLException("dup", "23000", 1062)).esFatal());
    }
}
