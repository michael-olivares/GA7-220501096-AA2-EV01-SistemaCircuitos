package co.sena.adso.circuitos.modelo;

/**
 * Representa la entidad PROYECTO del modelo relacional.
 * Tabla: proyecto — Dominio: Núcleo del Sistema
 *
 * Columnas mapeadas:
 *   id_proyecto, nombre, descripcion, tipo_circuito,
 *   estado, version_actual, id_usuario_creador
 *
 * GA7-220501096-AA2-EV01
 * Autor: Michael Ronald Olivares Giraldo — SENA Ficha 3118306
 */
public class Proyecto {

    // ── Atributos (mapean columnas de la tabla proyecto) ──────────
    private int    idProyecto;
    private String nombre;
    private String descripcion;
    private String tipoCircuito;    // ENUM: mixto|microcontrolador|asic|fpga|analogico
    private String estado;          // ENUM: activo|finalizado|archivado
    private String versionActual;
    private int    idUsuarioCreador;

    // ── Constructores ─────────────────────────────────────────────

    /** Constructor vacío requerido para operaciones de consulta. */
    public Proyecto() {}

    /**
     * Constructor completo para inserción de nuevos proyectos.
     * @param nombre           Nombre descriptivo del proyecto
     * @param descripcion      Descripción técnica del alcance
     * @param tipoCircuito     Tipo de circuito según ENUM de la BD
     * @param estado           Estado actual del proyecto
     * @param versionActual    Versión semántica actual
     * @param idUsuarioCreador ID del usuario que crea el proyecto
     */
    public Proyecto(String nombre, String descripcion, String tipoCircuito,
                    String estado, String versionActual, int idUsuarioCreador) {
        this.nombre           = nombre;
        this.descripcion      = descripcion;
        this.tipoCircuito     = tipoCircuito;
        this.estado           = estado;
        this.versionActual    = versionActual;
        this.idUsuarioCreador = idUsuarioCreador;
    }

    // ── Getters y Setters ─────────────────────────────────────────

    public int    getIdProyecto()        { return idProyecto; }
    public void   setIdProyecto(int id)  { this.idProyecto = id; }

    public String getNombre()            { return nombre; }
    public void   setNombre(String n)    { this.nombre = n; }

    public String getDescripcion()               { return descripcion; }
    public void   setDescripcion(String d)        { this.descripcion = d; }

    public String getTipoCircuito()              { return tipoCircuito; }
    public void   setTipoCircuito(String t)      { this.tipoCircuito = t; }

    public String getEstado()                    { return estado; }
    public void   setEstado(String e)            { this.estado = e; }

    public String getVersionActual()             { return versionActual; }
    public void   setVersionActual(String v)     { this.versionActual = v; }

    public int    getIdUsuarioCreador()          { return idUsuarioCreador; }
    public void   setIdUsuarioCreador(int id)    { this.idUsuarioCreador = id; }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Proyecto{id=%d | nombre='%s' | tipo='%s' | estado='%s' | version='%s' | creador=%d}",
            idProyecto, nombre, tipoCircuito, estado, versionActual, idUsuarioCreador
        );
    }
}
