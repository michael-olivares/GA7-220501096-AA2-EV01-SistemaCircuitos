package co.sena.adso.circuitos.dao;

import co.sena.adso.circuitos.conexion.ConexionBD;
import co.sena.adso.circuitos.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acceso a Datos (DAO) para la entidad USUARIO.
 * Implementa CRUD completo con PreparedStatement de JDBC.
 *
 * GA7-220501096-AA2-EV01
 * Autor: Michael Ronald Olivares Giraldo — SENA Ficha 3118306
 */
public class UsuarioDAO {

    // ── Sentencias SQL ────────────────────────────────────────────
    private static final String SQL_INSERTAR =
        "INSERT INTO usuario (nombre, email, contrasena_hash, rol, nivel_acceso, estado) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR =
        "SELECT id_usuario, nombre, email, contrasena_hash, rol, nivel_acceso, estado " +
        "FROM usuario ORDER BY nombre";

    private static final String SQL_BUSCAR_ID =
        "SELECT id_usuario, nombre, email, contrasena_hash, rol, nivel_acceso, estado " +
        "FROM usuario WHERE id_usuario = ?";

    private static final String SQL_BUSCAR_EMAIL =
        "SELECT id_usuario, nombre, email, contrasena_hash, rol, nivel_acceso, estado " +
        "FROM usuario WHERE email = ?";

    private static final String SQL_ACTUALIZAR =
        "UPDATE usuario SET nombre = ?, email = ?, rol = ?, nivel_acceso = ?, estado = ? " +
        "WHERE id_usuario = ?";

    private static final String SQL_ACTUALIZAR_ESTADO =
        "UPDATE usuario SET estado = ? WHERE id_usuario = ?";

    private static final String SQL_ELIMINAR =
        "DELETE FROM usuario WHERE id_usuario = ?";

    // ── Referencia al gestor de conexión ──────────────────────────
    private final ConexionBD gestorConexion;

    public UsuarioDAO() {
        this.gestorConexion = ConexionBD.obtenerInstancia();
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════

    /**
     * Inserta un nuevo usuario en el sistema.
     * La contraseña debe recibirse ya hasheada (SHA-256 o bcrypt).
     * @param usuario Objeto Usuario con los datos a persistir
     * @return true si la inserción fue exitosa
     */
    public boolean insertarUsuario(Usuario usuario) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getContrasenaHash());
            ps.setString(4, usuario.getRol());
            ps.setString(5, usuario.getNivelAcceso());
            ps.setString(6, usuario.getEstado());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                ResultSet claves = ps.getGeneratedKeys();
                if (claves.next()) {
                    usuario.setIdUsuario(claves.getInt(1));
                    System.out.println("[UsuarioDAO] ✅ Usuario insertado con ID: " + usuario.getIdUsuario());
                }
                ps.close();
                return true;
            }
            ps.close();

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al insertar usuario: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════

    /**
     * Retorna todos los usuarios del sistema ordenados por nombre.
     * @return Lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            rs.close();
            ps.close();
            System.out.println("[UsuarioDAO] ℹ️  Usuarios encontrados: " + lista.size());

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un usuario por su clave primaria.
     * @param idUsuario ID del usuario a buscar
     * @return Objeto Usuario o null si no existe
     */
    public Usuario buscarPorId(int idUsuario) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_ID);
            ps.setInt(1, idUsuario);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario usuario = mapearFila(rs);
                rs.close();
                ps.close();
                return usuario;
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al buscar usuario ID=" + idUsuario + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un usuario por su correo electrónico (campo UNIQUE en la BD).
     * Usado para autenticación y verificación de duplicados.
     * @param email Correo electrónico a buscar
     * @return Objeto Usuario o null si no existe
     */
    public Usuario buscarPorEmail(String email) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_EMAIL);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario usuario = mapearFila(rs);
                rs.close();
                ps.close();
                return usuario;
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al buscar usuario por email: " + e.getMessage());
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════

    /**
     * Actualiza los datos de un usuario (excepto contraseña y fecha de registro).
     * @param usuario Objeto con los nuevos valores e ID del registro
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarUsuario(Usuario usuario) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR);

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getRol());
            ps.setString(4, usuario.getNivelAcceso());
            ps.setString(5, usuario.getEstado());
            ps.setInt(6, usuario.getIdUsuario());

            int filas = ps.executeUpdate();
            ps.close();

            if (filas > 0) {
                System.out.println("[UsuarioDAO] ✅ Usuario ID=" + usuario.getIdUsuario() + " actualizado.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al actualizar usuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cambia el estado de la cuenta de un usuario.
     * Operación usada para activar/suspender sin modificar otros campos.
     * @param idUsuario   ID del usuario
     * @param nuevoEstado 'activo', 'inactivo' o 'suspendido'
     * @return true si la operación fue exitosa
     */
    public boolean actualizarEstado(int idUsuario, String nuevoEstado) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR_ESTADO);

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);

            int filas = ps.executeUpdate();
            ps.close();

            if (filas > 0) {
                System.out.println("[UsuarioDAO] ✅ Estado usuario ID=" + idUsuario + " → " + nuevoEstado);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al actualizar estado: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════════════════════

    /**
     * Elimina un usuario del sistema.
     * RESTRICCIÓN: la BD rechaza la eliminación si el usuario tiene
     * proyectos o diseños activos (FK ON DELETE RESTRICT).
     * @param idUsuario ID del usuario a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarUsuario(int idUsuario) {
        try {
            Connection conn = gestorConexion.obtenerConexion();
            PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR);
            ps.setInt(1, idUsuario);

            int filas = ps.executeUpdate();
            ps.close();

            if (filas > 0) {
                System.out.println("[UsuarioDAO] ✅ Usuario ID=" + idUsuario + " eliminado.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] ❌ Error al eliminar usuario (puede tener registros dependientes): " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // Auxiliar — mapear ResultSet → Usuario
    // ══════════════════════════════════════════════════════════════

    private Usuario mapearFila(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setContrasenaHash(rs.getString("contrasena_hash"));
        usuario.setRol(rs.getString("rol"));
        usuario.setNivelAcceso(rs.getString("nivel_acceso"));
        usuario.setEstado(rs.getString("estado"));
        return usuario;
    }
}
