package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;
import com.vigilplus.sigma.excepcion.PermisoDenegadoException;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.Persona;
import com.vigilplus.sigma.modelo.Usuario;
import com.vigilplus.sigma.persistencia.CredencialDAO;
import com.vigilplus.sigma.persistencia.PersonaDAO;
import com.vigilplus.sigma.persistencia.Transaccion;

import java.time.LocalDateTime;
import java.util.List;

/** Controlador del caso de uso "Gestionar credenciales" (CU01, RF01). Solo administradores. */
public class ControladorCredenciales {

    private final CredencialDAO credencialDAO = new CredencialDAO();
    private final PersonaDAO personaDAO = new PersonaDAO();

    /** Alta de credencial permanente para una persona existente, habilitada en los puntos indicados. */
    public Credencial alta(Usuario usuario, String dni, String codigo, Credencial.Tipo tipo,
                           LocalDateTime desde, LocalDateTime hasta, List<Integer> puntos) throws SigmaException {
        exigirAdministrador(usuario);
        if (!hasta.isAfter(desde)) throw new DatoInvalidoException("La vigencia hasta debe ser posterior a la desde");
        if (puntos.isEmpty()) throw new DatoInvalidoException("Indique al menos un punto de acceso");
        return Transaccion.ejecutar(con -> {
            Persona persona = personaDAO.buscarPorDni(con, dni);
            if (persona == null) throw new DatoInvalidoException("No existe una persona con DNI " + dni);
            Credencial c = new Credencial(0, persona.getIdPersona(), codigo, tipo, desde, hasta,
                    Credencial.Estado.ACTIVA, false);
            credencialDAO.insertar(con, c);
            credencialDAO.habilitarPuntos(con, c.getIdCredencial(), puntos);
            return c;
        });
    }

    public boolean modificarVigencia(Usuario usuario, String codigo, LocalDateTime hasta) throws SigmaException {
        exigirAdministrador(usuario);
        return Transaccion.ejecutar(con -> credencialDAO.modificarVigencia(con, codigo, hasta) == 1);
    }

    public boolean suspender(Usuario usuario, String codigo) throws SigmaException {
        return cambiarEstado(usuario, codigo, Credencial.Estado.SUSPENDIDA);
    }

    public boolean reactivar(Usuario usuario, String codigo) throws SigmaException {
        return cambiarEstado(usuario, codigo, Credencial.Estado.ACTIVA);
    }

    /** Baja logica: se conserva la credencial para no perder la trazabilidad de sus eventos. */
    public boolean darDeBaja(Usuario usuario, String codigo) throws SigmaException {
        return cambiarEstado(usuario, codigo, Credencial.Estado.BAJA);
    }

    public boolean revocarPunto(Usuario usuario, String codigo, int idPunto) throws SigmaException {
        exigirAdministrador(usuario);
        return Transaccion.ejecutar(con -> credencialDAO.revocarPunto(con, codigo, idPunto) == 1);
    }

    private boolean cambiarEstado(Usuario usuario, String codigo, Credencial.Estado estado) throws SigmaException {
        exigirAdministrador(usuario);
        return Transaccion.ejecutar(con -> credencialDAO.cambiarEstado(con, codigo, estado) == 1);
    }

    private void exigirAdministrador(Usuario usuario) throws PermisoDenegadoException {
        if (usuario == null || !usuario.esAdministrador()) {
            throw new PermisoDenegadoException("Operacion permitida solo para administradores");
        }
    }
}
