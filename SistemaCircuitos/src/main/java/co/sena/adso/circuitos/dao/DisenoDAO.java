package co.sena.adso.circuitos.dao;

import co.sena.adso.circuitos.conexion.ConexionBD;
import co.sena.adso.circuitos.modelo.Diseno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acceso a Datos (DAO) para la entidad DISENO.
 * Implementa CRUD completo con PreparedStatement de JDBC.
 *
 * Operaciones:
 *   CREATE  → insertarDiseno()
 *   READ    → listarPorProyecto() | buscarPorId() | listarPorEstado()
 *   UPDATE  → actualizarEstado() | actualizarDiseno()
 *   DELETE  → eliminarDiseno()
 *
 * GA7-220501096-AA2-EV01
 * Autor: Michael Ronald Olivares Giraldo — SENA Ficha 3118306
 */
public class DisenoDAO {

    // ── Sentencias SQL ────────────────────────────────────────────
    private static final String SQL_INSERTAR =
        "INSERT INTO diseno (nombre_diseno, tipo, formato, estado, cifrado, id_proyecto, id_usuario_creador) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR_POR_PROYECTO =
        "SELECT id_diseno, nombre_diseno, tipo, formato, estado, cifrado, id_proyecto, id_usuario_creador " +
        "FROM diseno WHERE id_proyecto = ? ORDER BY fecha_creacion DESC";

    private static final String SQL_BUSCAR_ID =
        "SELECT id_diseno, nombre_diseno, tipo, formato, estado, cifrado, id_proyecto, id_usuario_creador " +
        "FROM diseno WHERE id_diseno = ?";

    private static final String SQL_LISTAR_ESTADO =
        "SELECT id_diseno, nombre_diseno, tipo, formato, estado, cifrado, id_proyecto, id_usuario_creador " +
        "FROM diseno WHERE estado = ? ORDER BY nombre_diseno";

    private static final String SQL_ACTUALIZAR =
        "UPDATE diseno SET nombre_diseno = ?, tipo = ?, formato = ?, estado = ?, cifrado = ? " +
        "WHERE id_diseno = ?";

    private static final String SQL_ACTUALIZAR_ESTADO =
        "UPDATE diseno SET estado = ? WHERE id_diseno = ?";

    private static final String SQL_ELIMINAR =
        "DELETE FROM diseno WHERE id_diseno = ?";

    // ── Referencia al gestor de conexión ──────────────────────────
    private final ConexionBD gestorConexion;

    /** Constructor: obtiene la instancia Singleton del gestor de conexión. */
    public DisenoDAO() {
        this.gestorConexion = ConexionBD.obtenerInstancia();
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════

    /**
     * Inserta un nuevo diseño en la base de datos.
     * @param diseno Objeto Diseno con los datos a persistir
     * @return true si la inserción fue exitosa
     */
    public boolean insertarDiseno(Diseno diseno) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, diseno.getNombreDiseno());
            ps.setString(2, diseno.getTipo());
            ps.setString(3, diseno.getFormato());
            ps.setString(4, diseno.getEstado());
            ps.setBoolean(5, diseno.isCifrado());
            ps.setInt(6, diseno.getIdProyecto());
            ps.setInt(7, diseno.getIdUsuarioCreador());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                ResultSet claves = ps.getGeneratedKeys();
                if (claves.next()) {
                    diseno.setIdDiseno(claves.getInt(1));
                    System.out.println("[DisenoDAO] ✅ Diseño insertado con ID: " + diseno.getIdDiseno());
                }
                ps.close();
                return true;
            }
            ps.close();

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al insertar diseño: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════

    /**
     * Lista todos los diseños pertenecientes a un proyecto.
     * @param idProyecto ID del proyecto padre
     * @return Lista de diseños del proyecto
     */
    public List<Diseno> listarPorProyecto(int idProyecto) {
        List<Diseno> lista = new ArrayList<>();
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_LISTAR_POR_PROYECTO);
            ps.setInt(1, idProyecto);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            rs.close();
            ps.close();
            System.out.println("[DisenoDAO] ℹ️  Diseños del proyecto " + idProyecto + ": " + lista.size());

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al listar diseños por proyecto: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un diseño por su clave primaria.
     * @param idDiseno ID del diseño a buscar
     * @return Objeto Diseno o null si no existe
     */
    public Diseno buscarPorId(int idDiseno) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_ID);
            ps.setInt(1, idDiseno);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Diseno diseno = mapearFila(rs);
                rs.close();
                ps.close();
                return diseno;
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al buscar diseño ID=" + idDiseno + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista diseños filtrados por estado del flujo.
     * @param estado Valor ENUM: 'borrador', 'revision' o 'aprobado'
     * @return Lista de diseños en ese estado
     */
    public List<Diseno> listarPorEstado(String estado) {
        List<Diseno> lista = new ArrayList<>();
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_LISTAR_ESTADO);
            ps.setString(1, estado);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            rs.close();
            ps.close();
            System.out.println("[DisenoDAO] ℹ️  Diseños en estado '" + estado + "': " + lista.size());

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al listar por estado: " + e.getMessage());
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════

    /**
     * Actualiza todos los campos editables de un diseño.
     * @param diseno Objeto con los nuevos valores e ID del registro
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarDiseno(Diseno diseno) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR);

            ps.setString(1, diseno.getNombreDiseno());
            ps.setString(2, diseno.getTipo());
            ps.setString(3, diseno.getFormato());
            ps.setString(4, diseno.getEstado());
            ps.setBoolean(5, diseno.isCifrado());
            ps.setInt(6, diseno.getIdDiseno());

            int filas = ps.executeUpdate();
            ps.close();

            if (filas > 0) {
                System.out.println("[DisenoDAO] ✅ Diseño ID=" + diseno.getIdDiseno() + " actualizado.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al actualizar diseño: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza únicamente el estado del flujo de un diseño.
     * Operación liviana sin afectar otros campos.
     * @param idDiseno ID del diseño a actualizar
     * @param nuevoEstado Nuevo estado: 'borrador', 'revision' o 'aprobado'
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarEstado(int idDiseno, String nuevoEstado) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR_ESTADO);

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idDiseno);

            int filas = ps.executeUpdate();
            ps.close();

            if (filas > 0) {
                System.out.println("[DisenoDAO] ✅ Estado del diseño ID=" + idDiseno + " → " + nuevoEstado);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al actualizar estado: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════════════════════

    /**
     * Elimina un diseño por su ID.
     * Las versiones y simulaciones asociadas se eliminan por CASCADE.
     * @param idDiseno ID del diseño a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarDiseno(int idDiseno) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR);
            ps.setInt(1, idDiseno);

            int filas = ps.executeUpdate();
            ps.close();

            if (filas > 0) {
                System.out.println("[DisenoDAO] ✅ Diseño ID=" + idDiseno + " eliminado.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[DisenoDAO] ❌ Error al eliminar diseño: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // Auxiliar — mapear ResultSet → Diseno
    // ══════════════════════════════════════════════════════════════

    /**
     * Convierte la fila actual del ResultSet en un objeto Diseno.
     * @param rs ResultSet posicionado en la fila a mapear
     * @return Objeto Diseno poblado
     * @throws SQLException si la columna no existe
     */
    private Diseno mapearFila(ResultSet rs) throws SQLException {
        Diseno diseno = new Diseno();
        diseno.setIdDiseno(rs.getInt("id_diseno"));
        diseno.setNombreDiseno(rs.getString("nombre_diseno"));
        diseno.setTipo(rs.getString("tipo"));
        diseno.setFormato(rs.getString("formato"));
        diseno.setEstado(rs.getString("estado"));
        diseno.setCifrado(rs.getBoolean("cifrado"));
        diseno.setIdProyecto(rs.getInt("id_proyecto"));
        diseno.setIdUsuarioCreador(rs.getInt("id_usuario_creador"));
        return diseno;
    }
}
