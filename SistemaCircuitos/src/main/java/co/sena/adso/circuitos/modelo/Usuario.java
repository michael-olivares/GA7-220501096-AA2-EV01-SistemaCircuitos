package co.sena.adso.circuitos.modelo;

/**
 * Representa la entidad USUARIO del modelo relacional.
 * Tabla: usuario — Dominio: Núcleo del Sistema
 *
 * Columnas mapeadas:
 *   id_usuario, nombre, email, rol, nivel_acceso, estado
 *
 * NOTA: contrasena_hash no se expone en toString() por seguridad.
 *
 * GA7-220501096-AA2-EV01
 * Autor: Michael Ronald Olivares Giraldo — SENA Ficha 3118306
 */
public class Usuario {

    // ── Atributos ─────────────────────────────────────────────────
    private int    idUsuario;
    private String nombre;
    private String email;
    private String contrasenaHash;  // SHA-256 / bcrypt — no exponer en logs
    private String rol;             // ENUM: disenador|administrador|colaborador
    private String nivelAcceso;
    private String estado;          // ENUM: activo|inactivo|suspendido

    // ── Constructores ─────────────────────────────────────────────

    /** Constructor vacío para operaciones de consulta. */
    public Usuario() {}

    /**
     * Constructor completo para registro de nuevos usuarios.
     * @param nombre        Nombre completo del usuario
     * @param email         Correo corporativo (único en la BD)
     * @param contrasenaHash Hash de la contraseña (SHA-256)
     * @param rol           Rol del usuario en el sistema
     * @param nivelAcceso   Nivel de acceso asignado
     * @param estado        Estado de la cuenta
     */
    public Usuario(String nombre, String email, String contrasenaHash,
                   String rol, String nivelAcceso, String estado) {
        this.nombre        = nombre;
        this.email         = email;
        this.contrasenaHash = contrasenaHash;
        this.rol           = rol;
        this.nivelAcceso   = nivelAcceso;
        this.estado        = estado;
    }

    // ── Getters y Setters ─────────────────────────────────────────

    public int    getIdUsuario()              { return idUsuario; }
    public void   setIdUsuario(int id)        { this.idUsuario = id; }

    public String getNombre()                 { return nombre; }
    public void   setNombre(String n)         { this.nombre = n; }

    public String getEmail()                  { return email; }
    public void   setEmail(String e)          { this.email = e; }

    public String getContrasenaHash()         { return contrasenaHash; }
    public void   setContrasenaHash(String h) { this.contrasenaHash = h; }

    public String getRol()                    { return rol; }
    public void   setRol(String r)            { this.rol = r; }

    public String getNivelAcceso()            { return nivelAcceso; }
    public void   setNivelAcceso(String n)    { this.nivelAcceso = n; }

    public String getEstado()                 { return estado; }
    public void   setEstado(String e)         { this.estado = e; }

    // ── toString — omite hash por seguridad ───────────────────────
    @Override
    public String toString() {
        return String.format(
            "Usuario{id=%d | nombre='%s' | email='%s' | rol='%s' | nivel='%s' | estado='%s'}",
            idUsuario, nombre, email, rol, nivelAcceso, estado
        );
    }
}
