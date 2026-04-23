package co.sena.adso.circuitos.modelo;

/**
 * Representa la entidad DISENO del modelo relacional.
 * Tabla: diseno — Dominio: Núcleo del Sistema
 *
 * Columnas mapeadas:
 *   id_diseno, nombre_diseno, tipo, formato, estado,
 *   cifrado, id_proyecto, id_usuario_creador
 *
 * GA7-220501096-AA2-EV01
 * Autor: Michael Ronald Olivares Giraldo — SENA Ficha 3118306
 */
public class Diseno {

    // ── Atributos (mapean columnas de la tabla diseno) ────────────
    private int     idDiseno;
    private String  nombreDiseno;
    private String  tipo;           // ENUM: esquematico|pcb|layout
    private String  formato;        // KiCad 7.0, Altium 23, Cadence, etc.
    private String  estado;         // ENUM: borrador|revision|aprobado
    private boolean cifrado;
    private int     idProyecto;
    private int     idUsuarioCreador;

    // ── Constructores ─────────────────────────────────────────────

    /** Constructor vacío requerido para operaciones de consulta. */
    public Diseno() {}

    /**
     * Constructor completo para inserción de nuevos diseños.
     * @param nombreDiseno       Nombre descriptivo del archivo de diseño
     * @param tipo               Tipo según ENUM: esquematico|pcb|layout
     * @param formato            Herramienta EDA utilizada
     * @param estado             Estado de flujo del diseño
     * @param cifrado            Indica si el archivo está cifrado
     * @param idProyecto         ID del proyecto al que pertenece
     * @param idUsuarioCreador   ID del diseñador responsable
     */
    public Diseno(String nombreDiseno, String tipo, String formato,
                  String estado, boolean cifrado, int idProyecto, int idUsuarioCreador) {
        this.nombreDiseno     = nombreDiseno;
        this.tipo             = tipo;
        this.formato          = formato;
        this.estado           = estado;
        this.cifrado          = cifrado;
        this.idProyecto       = idProyecto;
        this.idUsuarioCreador = idUsuarioCreador;
    }

    // ── Getters y Setters ─────────────────────────────────────────

    public int     getIdDiseno()              { return idDiseno; }
    public void    setIdDiseno(int id)        { this.idDiseno = id; }

    public String  getNombreDiseno()          { return nombreDiseno; }
    public void    setNombreDiseno(String n)  { this.nombreDiseno = n; }

    public String  getTipo()                  { return tipo; }
    public void    setTipo(String t)          { this.tipo = t; }

    public String  getFormato()               { return formato; }
    public void    setFormato(String f)       { this.formato = f; }

    public String  getEstado()                { return estado; }
    public void    setEstado(String e)        { this.estado = e; }

    public boolean isCifrado()                { return cifrado; }
    public void    setCifrado(boolean c)      { this.cifrado = c; }

    public int     getIdProyecto()            { return idProyecto; }
    public void    setIdProyecto(int id)      { this.idProyecto = id; }

    public int     getIdUsuarioCreador()         { return idUsuarioCreador; }
    public void    setIdUsuarioCreador(int id)   { this.idUsuarioCreador = id; }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Diseno{id=%d | nombre='%s' | tipo='%s' | formato='%s' | estado='%s' | cifrado=%b | proyecto=%d}",
            idDiseno, nombreDiseno, tipo, formato, estado, cifrado, idProyecto
        );
    }
}
