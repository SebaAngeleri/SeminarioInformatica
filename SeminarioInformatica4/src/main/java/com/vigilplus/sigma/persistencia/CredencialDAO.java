package com.vigilplus.sigma.persistencia;

import com.vigilplus.sigma.modelo.Credencial;
import com.vigilplus.sigma.modelo.EntradaPadron;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/** DAO de la entidad Credencial y de la relacion credencial_punto_acceso. */
public class CredencialDAO extends DaoBase<Credencial> implements Dao<Credencial, Integer> {

    private static final String SELECT = "SELECT id_credencial, id_persona, codigo, tipo, vigencia_desde, "
            + "vigencia_hasta, estado, temporal FROM credencial ";

    @Override
    protected Credencial mapear(ResultSet rs) throws SQLException {
        return new Credencial(rs.getInt("id_credencial"), rs.getInt("id_persona"), rs.getString("codigo"),
                Credencial.Tipo.valueOf(rs.getString("tipo")), fecha(rs, "vigencia_desde"),
                fecha(rs, "vigencia_hasta"), Credencial.Estado.valueOf(rs.getString("estado")),
                rs.getBoolean("temporal"));
    }

    @Override
    public Credencial buscarPorId(Connection con, Integer id) throws SQLException {
        return consultarUno(con, SELECT + "WHERE id_credencial = ?", id);
    }

    @Override
    public List<Credencial> listarTodos(Connection con) throws SQLException {
        return consultarLista(con, SELECT + "ORDER BY codigo");
    }

    public Credencial buscarPorCodigo(Connection con, String codigo) throws SQLException {
        return consultarUno(con, SELECT + "WHERE codigo = ?", codigo);
    }

    public boolean estaHabilitadaParaPunto(Connection con, int idCredencial, int idPunto) throws SQLException {
        return existe(con, "SELECT 1 FROM credencial_punto_acceso WHERE id_credencial = ? AND id_punto_acceso = ?",
                idCredencial, idPunto);
    }

    public int insertar(Connection con, Credencial c) throws SQLException {
        long id = insertar(con, "INSERT INTO credencial (id_persona, codigo, tipo, vigencia_desde, vigencia_hasta, "
                        + "estado, temporal) VALUES (?, ?, ?, ?, ?, ?, ?)",
                c.getIdPersona(), c.getCodigo(), c.getTipo(), c.getVigenciaDesde(), c.getVigenciaHasta(),
                c.getEstado(), c.isTemporal());
        c.setIdCredencial((int) id);
        return c.getIdCredencial();
    }

    /** Habilita la credencial en varios puntos con una sola ida y vuelta a la base (batch). */
    public void habilitarPuntos(Connection con, int idCredencial, List<Integer> puntos) throws SQLException {
        String sql = "INSERT INTO credencial_punto_acceso (id_credencial, id_punto_acceso) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int idPunto : puntos) {
                ps.setInt(1, idCredencial);
                ps.setInt(2, idPunto);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int revocarPunto(Connection con, String codigo, int idPunto) throws SQLException {
        return actualizar(con, "DELETE cpa FROM credencial_punto_acceso cpa "
                + "JOIN credencial c ON c.id_credencial = cpa.id_credencial "
                + "WHERE c.codigo = ? AND cpa.id_punto_acceso = ?", codigo, idPunto);
    }

    public int cambiarEstado(Connection con, String codigo, Credencial.Estado estado) throws SQLException {
        return actualizar(con, "UPDATE credencial SET estado = ? WHERE codigo = ?", estado, codigo);
    }

    public int modificarVigencia(Connection con, String codigo, LocalDateTime hasta) throws SQLException {
        return actualizar(con, "UPDATE credencial SET vigencia_hasta = ? WHERE codigo = ? AND vigencia_desde < ?",
                hasta, codigo, hasta);
    }

    /**
     * Padron de credenciales activas de un complejo, con los puntos habilitados
     * de ese complejo, para validar accesos sin conexion.
     */
    public List<EntradaPadron> padronDeComplejo(Connection con, int idComplejo) throws SQLException {
        String sql = "SELECT c.codigo, CONCAT(p.apellido, ', ', p.nombre) AS titular, c.tipo, c.estado, "
                   + "       c.vigencia_desde, c.vigencia_hasta, "
                   + "       GROUP_CONCAT(cpa.id_punto_acceso ORDER BY cpa.id_punto_acceso SEPARATOR ',') AS puntos "
                   + "FROM credencial c "
                   + "JOIN persona p ON p.id_persona = c.id_persona "
                   + "JOIN credencial_punto_acceso cpa ON cpa.id_credencial = c.id_credencial "
                   + "JOIN punto_acceso pa ON pa.id_punto_acceso = cpa.id_punto_acceso "
                   + "WHERE pa.id_complejo = ? AND c.estado <> 'BAJA' AND c.vigencia_hasta >= NOW() "
                   + "GROUP BY c.id_credencial, c.codigo, p.apellido, p.nombre, c.tipo, c.estado, "
                   + "         c.vigencia_desde, c.vigencia_hasta "
                   + "ORDER BY c.codigo";
        return consultar(con, sql, rs -> {
            String[] ids = rs.getString("puntos").split(",");
            int[] puntos = new int[ids.length];
            for (int i = 0; i < ids.length; i++) {
                puntos[i] = Integer.parseInt(ids[i]);
            }
            return new EntradaPadron(rs.getString("codigo"), rs.getString("titular"),
                    Credencial.Tipo.valueOf(rs.getString("tipo")), Credencial.Estado.valueOf(rs.getString("estado")),
                    fecha(rs, "vigencia_desde"), fecha(rs, "vigencia_hasta"), puntos);
        }, idComplejo);
    }
}
