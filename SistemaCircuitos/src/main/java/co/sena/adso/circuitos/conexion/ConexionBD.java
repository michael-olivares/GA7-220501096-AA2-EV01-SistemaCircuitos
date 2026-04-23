package co.sena.adso.circuitos.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexión JDBC al servidor MySQL.
 * Implementa el patrón Singleton para garantizar una única instancia de conexión.
 *
 * Sistema de Diseño Integrado de Circuitos
 * GA7-220501096-AA2-EV01 — Codificación de Módulos con JDBC
 * Autor: Michael Ronald Olivares Giraldo
 * SENA — Ficha 3118306 — Abril 2026
 */
public class ConexionBD {

    // ── Parámetros de conexión ─────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/sistema_diseno_circuitos"
                                         + "?useSSL=false&serverTimezone=America/Bogota"
                                         + "&useUnicode=true&characterEncoding=UTF-8"
                                         + "&allowPublicKeyRetrieval=true";
    private static final String USUARIO  = "root";
    private static final String CLAVE    = "root";
    private static final String DRIVER   = "com.mysql.cj.jdbc.Driver";

    // ── Instancia Singleton ────────────────────────────────────────
    private static ConexionBD instancia;
    private Connection conexion;

    /**
     * Constructor privado — carga el driver JDBC al inicializar.
     */
    private ConexionBD() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[ConexionBD] Driver MySQL no encontrado: " + e.getMessage());
        }
    }

    /**
     * Retorna la instancia única del gestor de conexión.
     * @return instancia de ConexionBD
     */
    public static ConexionBD obtenerInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /**
     * Abre y retorna una conexión activa a la base de datos.
     * Si la conexión ya existe y está abierta, la reutiliza.
     * @return Connection activo con sistema_diseno_circuitos
     * @throws SQLException si la conexión falla
     */
    public Connection obtenerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("[ConexionBD] Conexión establecida con sistema_diseno_circuitos");
        }
        return conexion;
    }

    /**
     * Cierra la conexión activa de forma segura.
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("[ConexionBD] Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("[ConexionBD] Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
