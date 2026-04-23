package co.sena.adso.circuitos.dao;

import co.sena.adso.circuitos.conexion.ConexionBD;
import co.sena.adso.circuitos.modelo.Proyecto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acceso a Datos (DAO) para la entidad PROYECTO.
 * Implementa las cuatro operaciones CRUD mediante PreparedStatement de JDBC:
 *   - CREATE  → insertarProyecto()
 *   - READ    → listarProyectos() | buscarPorId() | buscarPorEstado()
 *   - UPDATE  → actualizarProyecto()
 *   - DELETE  → eliminarProyecto()
 *
 * Todas las operaciones usan PreparedStatement para prevenir inyección SQL.
 * El manejo de excepciones registra errores sin propagar SQLException al caller.
 *
 * GA7-220501096-AA2-EV01
 * Autor: Michael Ronald Olivares Giraldo — SENA Ficha 3118306
 */
public class ProyectoDAO {

    // ── Sentencias SQL parametrizadas ─────────────────────────────
    private static final String SQL_INSERTAR =
        "INSERT INTO proyecto (nombre, descripcion, tipo_circuito, estado, version_actual, id_usuario_creador) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR =
        "SELECT id_proyecto, nombre, descripcion, tipo_circuito, estado, version_actual, id_usuario_creador " +
        "FROM proyecto ORDER BY fecha_creacion DESC";

    private static final String SQL_BUSCAR_ID =
        "SELECT id_proyecto, nombre, descripcion, tipo_circuito, estado, version_actual, id_usuario_creador " +
        "FROM proyecto WHERE id_proyecto = ?";

    private static final String SQL_BUSCAR_ESTADO =
        "SELECT id_proyecto, nombre, descripcion, tipo_circuito, estado, version_actual, id_usuario_creador " +
        "FROM proyecto WHERE estado = ? ORDER BY nombre";

    private static final String SQL_ACTUALIZAR =
        "UPDATE proyecto SET nombre = ?, descripcion = ?, tipo_circuito = ?, " +
        "estado = ?, version_actual = ? WHERE id_proyecto = ?";

    private static final String SQL_ELIMINAR =
        "DELETE FROM proyecto WHERE id_proyecto = ?";

    // ── Referencia al gestor de conexión ──────────────────────────
    private final ConexionBD gestorConexion;

    /** Constructor: obtiene la instancia Singleton del gestor de conexión. */
    public ProyectoDAO() {
        this.gestorConexion = ConexionBD.obtenerInstancia();
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE — Insertar nuevo proyecto
    // ══════════════════════════════════════════════════════════════

    /**
     * Inserta un nuevo proyecto en la base de datos.
     * Recupera el ID generado automáticamente por AUTO_INCREMENT.
     * @param proyecto Objeto Proyecto con los datos a insertar
     * @return true si la inserción fue exitosa, false en caso de error
     */
    public boolean insertarProyecto(Proyecto proyecto) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, proyecto.getNombre());
            ps.setString(2, proyecto.getDescripcion());
            ps.setString(3, proyecto.getTipoCircuito());
            ps.setString(4, proyecto.getEstado());
            ps.setString(5, proyecto.getVersionActual());
            ps.setInt(6, proyecto.getIdUsuarioCreador());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet claves = ps.getGeneratedKeys();
                if (claves.next()) {
                    proyecto.setIdProyecto(claves.getInt(1));
                    System.out.println("[ProyectoDAO] ✅ Proyecto insertado con ID: " + proyecto.getIdProyecto());
                }
                ps.close();
                return true;
            }
            ps.close();

        } catch (SQLException e) {
            System.err.println("[ProyectoDAO] ❌ Error al insertar proyecto: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // READ — Consultar proyectos
    // ══════════════════════════════════════════════════════════════

    /**
     * Retorna todos los proyectos ordenados por fecha de creación descendente.
     * @return Lista de proyectos; lista vacía si no hay registros o hay error
     */
    public List<Proyecto> listarProyectos() {
        List<Proyecto> lista = new ArrayList<>();
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            rs.close();
            ps.close();
            System.out.println("[ProyectoDAO] ℹ️  Proyectos encontrados: " + lista.size());

        } catch (SQLException e) {
            System.err.println("[ProyectoDAO] ❌ Error al listar proyectos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un proyecto por su clave primaria.
     * @param idProyecto ID del proyecto a buscar
     * @return Objeto Proyecto encontrado, o null si no existe
     */
    public Proyecto buscarPorId(int idProyecto) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_ID);
            ps.setInt(1, idProyecto);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Proyecto proyecto = mapearFila(rs);
                rs.close();
                ps.close();
                return proyecto;
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("[ProyectoDAO] ❌ Error al buscar proyecto ID=" + idProyecto + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista proyectos filtrados por estado.
     * @param estado Valor ENUM: 'activo', 'finalizado' o 'archivado'
     * @return Lista de proyectos con el estado especificado
     */
    public List<Proyecto> buscarPorEstado(String estado) {
        List<Proyecto> lista = new ArrayList<>();
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_ESTADO);
            ps.setString(1, estado);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            rs.close();
            ps.close();
            System.out.println("[ProyectoDAO] ℹ️  Proyectos con estado '" + estado + "': " + lista.size());

        } catch (SQLException e) {
            System.err.println("[ProyectoDAO] ❌ Error al buscar por estado: " + e.getMessage());
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE — Actualizar proyecto existente
    // ══════════════════════════════════════════════════════════════

    /**
     * Actualiza los campos editables de un proyecto existente.
     * El id_usuario_creador y la fecha_creacion no se modifican.
     * @param proyecto Objeto con los nuevos valores y el ID del registro a actualizar
     * @return true si la actualización fue exitosa, false si no se encontró el ID
     */
    public boolean actualizarProyecto(Proyecto proyecto) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR);

            ps.setString(1, proyecto.getNombre());
            ps.setString(2, proyecto.getDescripcion());
            ps.setString(3, proyecto.getTipoCircuito());
            ps.setString(4, proyecto.getEstado());
            ps.setString(5, proyecto.getVersionActual());
            ps.setInt(6, proyecto.getIdProyecto());

            int filasAfectadas = ps.executeUpdate();
            ps.close();

            if (filasAfectadas > 0) {
                System.out.println("[ProyectoDAO] ✅ Proyecto ID=" + proyecto.getIdProyecto() + " actualizado.");
                return true;
            } else {
                System.out.println("[ProyectoDAO] ⚠️  No se encontró proyecto con ID=" + proyecto.getIdProyecto());
            }

        } catch (SQLException e) {
            System.err.println("[ProyectoDAO] ❌ Error al actualizar proyecto: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE — Eliminar proyecto
    // ══════════════════════════════════════════════════════════════

    /**
     * Elimina un proyecto por su ID.
     * PRECAUCIÓN: eliminación en CASCADE borrará los diseños,
     * versiones, simulaciones y documentación asociados (según FK del modelo).
     * @param idProyecto ID del proyecto a eliminar
     * @return true si se eliminó correctamente, false si no existía
     */
    public boolean eliminarProyecto(int idProyecto) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR);
            ps.setInt(1, idProyecto);

            int filasAfectadas = ps.executeUpdate();
            ps.close();

            if (filasAfectadas > 0) {
                System.out.println("[ProyectoDAO] ✅ Proyecto ID=" + idProyecto + " eliminado.");
                return true;
            } else {
                System.out.println("[ProyectoDAO] ⚠️  No se encontró proyecto con ID=" + idProyecto);
            }

        } catch (SQLException e) {
            System.err.println("[ProyectoDAO] ❌ Error al eliminar proyecto: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // Método auxiliar — mapear fila ResultSet → Proyecto
    // ══════════════════════════════════════════════════════════════

    /**
     * Convierte la fila actual del ResultSet en un objeto Proyecto.
     * Método privado de uso interno; no expuesto como API pública.
     * @param rs ResultSet posicionado en la fila a mapear
     * @return Objeto Proyecto poblado con los valores de la fila
     * @throws SQLException si la columna no existe en el ResultSet
     */
    private Proyecto mapearFila(ResultSet rs) throws SQLException {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(rs.getInt("id_proyecto"));
        proyecto.setNombre(rs.getString("nombre"));
        proyecto.setDescripcion(rs.getString("descripcion"));
        proyecto.setTipoCircuito(rs.getString("tipo_circuito"));
        proyecto.setEstado(rs.getString("estado"));
        proyecto.setVersionActual(rs.getString("version_actual"));
        proyecto.setIdUsuarioCreador(rs.getInt("id_usuario_creador"));
        return proyecto;
    }
}
