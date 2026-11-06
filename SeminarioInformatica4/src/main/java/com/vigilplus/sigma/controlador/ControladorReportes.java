package com.vigilplus.sigma.controlador;

import com.vigilplus.sigma.excepcion.DatoInvalidoException;
import com.vigilplus.sigma.excepcion.SigmaException;
import com.vigilplus.sigma.modelo.Alerta;
import com.vigilplus.sigma.modelo.EntradaPadron;
import com.vigilplus.sigma.modelo.PadronLocal;
import com.vigilplus.sigma.modelo.RegistroHistorial;
import com.vigilplus.sigma.persistencia.AlertaDAO;
import com.vigilplus.sigma.persistencia.CredencialDAO;
import com.vigilplus.sigma.persistencia.EventoAccesoDAO;
import com.vigilplus.sigma.persistencia.PuntoAccesoDAO;
import com.vigilplus.sigma.persistencia.Transaccion;
import com.vigilplus.sigma.reporte.ResumenAlertas;
import com.vigilplus.sigma.util.ArchivoCsv;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de reportes y archivos: resumen de alertas, exportacion del
 * historial para auditorias y padron local para el modo sin conexion.
 */
public class ControladorReportes {

    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final EventoAccesoDAO eventoDAO = new EventoAccesoDAO();
    private final CredencialDAO credencialDAO = new CredencialDAO();
    private final PuntoAccesoDAO puntoDAO = new PuntoAccesoDAO();

    public ResumenAlertas resumenAlertas() throws SigmaException {
        List<Alerta> pendientes = Transaccion.consultar(alertaDAO::listarPendientes);
        return new ResumenAlertas(pendientes);
    }

    /**
     * Exporta el historial a un archivo CSV para entregarlo en una auditoria (RF06).
     * @return cantidad de registros exportados
     */
    public int exportarHistorial(Path archivo, String dni, LocalDateTime desde, LocalDateTime hasta)
            throws SigmaException {
        if (hasta.isBefore(desde)) throw new DatoInvalidoException("Rango de fechas invalido");
        List<RegistroHistorial> filas = Transaccion.consultar(con -> eventoDAO.historial(con, dni, null, desde, hasta));
        if (filas.isEmpty()) throw new DatoInvalidoException("No hay accesos en ese rango para exportar");
        return ArchivoCsv.escribir(archivo, filas);
    }

    /**
     * Genera el archivo de padron de un complejo, que el controlador local
     * guarda para seguir validando accesos si pierde la conexion.
     * @return cantidad de credenciales exportadas
     */
    public int exportarPadron(Path archivo, int idComplejo) throws SigmaException {
        List<EntradaPadron> entradas = Transaccion.consultar(con -> {
            if (!puntoDAO.existeComplejo(con, idComplejo)) {
                throw new DatoInvalidoException("No existe el complejo " + idComplejo);
            }
            return credencialDAO.padronDeComplejo(con, idComplejo);
        });
        if (entradas.isEmpty()) throw new DatoInvalidoException("El complejo no tiene credenciales vigentes");
        return ArchivoCsv.escribir(archivo, entradas);
    }

    /** Lee un archivo de padron y lo deja listo para verificar accesos sin usar la base de datos. */
    public PadronLocal cargarPadron(Path archivo) throws SigmaException {
        List<String[]> lineas = ArchivoCsv.leer(archivo);
        List<EntradaPadron> entradas = new ArrayList<>(lineas.size());
        for (int i = 0; i < lineas.size(); i++) {
            entradas.add(EntradaPadron.desdeCampos(lineas.get(i), i + 2));   // +2: encabezado y base 1
        }
        return new PadronLocal(entradas);
    }
}
