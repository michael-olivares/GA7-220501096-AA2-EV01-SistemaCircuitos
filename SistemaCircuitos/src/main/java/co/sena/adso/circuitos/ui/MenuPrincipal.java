package co.sena.adso.circuitos.ui;

import co.sena.adso.circuitos.conexion.ConexionBD;
import co.sena.adso.circuitos.dao.DisenoDAO;
import co.sena.adso.circuitos.dao.ProyectoDAO;
import co.sena.adso.circuitos.dao.UsuarioDAO;
import co.sena.adso.circuitos.modelo.Diseno;
import co.sena.adso.circuitos.modelo.Proyecto;
import co.sena.adso.circuitos.modelo.Usuario;

import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada de la aplicación.
 * Presenta un menú de consola interactivo para demostrar las operaciones
 * CRUD (Insertar, Consultar, Actualizar, Eliminar) sobre las entidades
 * principales del Sistema de Diseño Integrado de Circuitos.
 *
 * Módulos demostrados:
 *   1. Gestión de Proyectos  (ProyectoDAO)
 *   2. Gestión de Diseños    (DisenoDAO)
 *   3. Gestión de Usuarios   (UsuarioDAO)
 *
 * Conexión: JDBC → MySQL 8.x → sistema_diseno_circuitos
 *
 * GA7-220501096-AA2-EV01 — Codificación de Módulos del Software
 * Autor: Michael Ronald Olivares Giraldo
 * SENA — Ficha 3118306 — Abril 2026
 */
public class MenuPrincipal {

    // ── Constantes de cabecera ─────────────────────────────────────
    private static final String LINEA   = "═".repeat(60);
    private static final String DIVISOR = "─".repeat(60);

    // ── DAOs compartidos por todos los módulos ────────────────────
    private static final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private static final DisenoDAO   disenoDAO   = new DisenoDAO();
    private static final UsuarioDAO  usuarioDAO  = new UsuarioDAO();
    private static final Scanner     scanner     = new Scanner(System.in);

    // ══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ══════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        imprimirBanner();

        boolean ejecutando = true;
        while (ejecutando) {
            imprimirMenuPrincipal();
            int opcion = leerEntero("Seleccione módulo: ");

            switch (opcion) {
                case 1 -> menuProyectos();
                case 2 -> menuDisenos();
                case 3 -> menuUsuarios();
                case 4 -> ejecutarDemostracionCompleta();
                case 0 -> {
                    System.out.println("\n⚡ Cerrando conexión y saliendo del sistema...");
                    ConexionBD.obtenerInstancia().cerrarConexion();
                    System.out.println("✅ Hasta pronto. — Michael Ronald Olivares Giraldo");
                    ejecutando = false;
                }
                default -> System.out.println("⚠️  Opción no válida. Intente nuevamente.");
            }
        }
        scanner.close();
    }

    // ══════════════════════════════════════════════════════════════
    // MENÚ PRINCIPAL
    // ══════════════════════════════════════════════════════════════

    private static void imprimirBanner() {
        System.out.println();
        System.out.println(LINEA);
        System.out.println("  ⚡ SISTEMA DE DISEÑO INTEGRADO DE CIRCUITOS");
        System.out.println("  GA7-220501096-AA2-EV01 — Módulos CRUD con JDBC");
        System.out.println("  Autor: Michael Ronald Olivares Giraldo");
        System.out.println("  SENA — Ficha 3118306 — Abril 2026");
        System.out.println(LINEA);
    }

    private static void imprimirMenuPrincipal() {
        System.out.println("\n" + LINEA);
        System.out.println("  MENÚ PRINCIPAL");
        System.out.println(DIVISOR);
        System.out.println("  [1] Gestión de Proyectos");
        System.out.println("  [2] Gestión de Diseños");
        System.out.println("  [3] Gestión de Usuarios");
        System.out.println("  [4] Demostración CRUD completa");
        System.out.println("  [0] Salir");
        System.out.println(LINEA);
    }

    // ══════════════════════════════════════════════════════════════
    // MÓDULO 1 — GESTIÓN DE PROYECTOS
    // ══════════════════════════════════════════════════════════════

    private static void menuProyectos() {
        boolean enMenu = true;
        while (enMenu) {
            System.out.println("\n" + DIVISOR);
            System.out.println("  📁 GESTIÓN DE PROYECTOS");
            System.out.println(DIVISOR);
            System.out.println("  [1] Listar todos los proyectos");
            System.out.println("  [2] Buscar proyecto por ID");
            System.out.println("  [3] Listar proyectos por estado");
            System.out.println("  [4] Crear nuevo proyecto (INSERT)");
            System.out.println("  [5] Actualizar proyecto (UPDATE)");
            System.out.println("  [6] Eliminar proyecto (DELETE)");
            System.out.println("  [0] Volver al menú principal");
            System.out.println(DIVISOR);

            int opcion = leerEntero("Seleccione operación: ");
            switch (opcion) {
                case 1 -> listarProyectos();
                case 2 -> buscarProyectoPorId();
                case 3 -> listarProyectosPorEstado();
                case 4 -> crearProyecto();
                case 5 -> actualizarProyecto();
                case 6 -> eliminarProyecto();
                case 0 -> enMenu = false;
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static void listarProyectos() {
        System.out.println("\n--- LISTADO DE PROYECTOS ---");
        List<Proyecto> proyectos = proyectoDAO.listarProyectos();
        if (proyectos.isEmpty()) {
            System.out.println("No hay proyectos registrados.");
            return;
        }
        proyectos.forEach(p -> System.out.println("  " + p));
    }

    private static void buscarProyectoPorId() {
        int id = leerEntero("ID del proyecto a buscar: ");
        Proyecto proyecto = proyectoDAO.buscarPorId(id);
        if (proyecto != null) {
            System.out.println("✅ Encontrado: " + proyecto);
        } else {
            System.out.println("⚠️  No se encontró proyecto con ID=" + id);
        }
    }

    private static void listarProyectosPorEstado() {
        System.out.println("Estados disponibles: activo | finalizado | archivado");
        String estado = leerTexto("Estado a filtrar: ");
        List<Proyecto> proyectos = proyectoDAO.buscarPorEstado(estado);
        if (proyectos.isEmpty()) {
            System.out.println("No hay proyectos con estado '" + estado + "'.");
            return;
        }
        proyectos.forEach(p -> System.out.println("  " + p));
    }

    private static void crearProyecto() {
        System.out.println("\n--- CREAR NUEVO PROYECTO ---");
        String nombre     = leerTexto("Nombre del proyecto: ");
        String desc       = leerTexto("Descripción: ");
        System.out.println("Tipos disponibles: mixto | microcontrolador | asic | fpga | analogico");
        String tipo       = leerTexto("Tipo de circuito: ");
        String version    = leerTexto("Versión inicial (ej. 1.0.0): ");
        int    idCreador  = leerEntero("ID del usuario creador: ");

        Proyecto nuevo = new Proyecto(nombre, desc, tipo, "activo", version, idCreador);
        boolean resultado = proyectoDAO.insertarProyecto(nuevo);
        System.out.println(resultado
            ? "✅ Proyecto creado con ID: " + nuevo.getIdProyecto()
            : "❌ No se pudo crear el proyecto.");
    }

    private static void actualizarProyecto() {
        System.out.println("\n--- ACTUALIZAR PROYECTO ---");
        int id = leerEntero("ID del proyecto a actualizar: ");
        Proyecto existente = proyectoDAO.buscarPorId(id);
        if (existente == null) {
            System.out.println("⚠️  Proyecto no encontrado.");
            return;
        }
        System.out.println("Proyecto actual: " + existente);

        String nombre  = leerTexto("Nuevo nombre [Enter para mantener]: ");
        String version = leerTexto("Nueva versión [Enter para mantener]: ");
        System.out.println("Estados disponibles: activo | finalizado | archivado");
        String estado  = leerTexto("Nuevo estado [Enter para mantener]: ");

        if (!nombre.isBlank())  existente.setNombre(nombre);
        if (!version.isBlank()) existente.setVersionActual(version);
        if (!estado.isBlank())  existente.setEstado(estado);

        boolean resultado = proyectoDAO.actualizarProyecto(existente);
        System.out.println(resultado ? "✅ Proyecto actualizado." : "❌ No se pudo actualizar.");
    }

    private static void eliminarProyecto() {
        System.out.println("\n--- ELIMINAR PROYECTO ---");
        int id = leerEntero("ID del proyecto a eliminar: ");
        System.out.print("⚠️  ¿Confirma eliminación del proyecto ID=" + id + "? (s/n): ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("s")) {
            boolean resultado = proyectoDAO.eliminarProyecto(id);
            System.out.println(resultado ? "✅ Proyecto eliminado." : "❌ No se pudo eliminar.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MÓDULO 2 — GESTIÓN DE DISEÑOS
    // ══════════════════════════════════════════════════════════════

    private static void menuDisenos() {
        boolean enMenu = true;
        while (enMenu) {
            System.out.println("\n" + DIVISOR);
            System.out.println("  🔌 GESTIÓN DE DISEÑOS");
            System.out.println(DIVISOR);
            System.out.println("  [1] Listar diseños de un proyecto");
            System.out.println("  [2] Buscar diseño por ID");
            System.out.println("  [3] Listar diseños por estado");
            System.out.println("  [4] Crear nuevo diseño (INSERT)");
            System.out.println("  [5] Actualizar diseño (UPDATE)");
            System.out.println("  [6] Cambiar estado de diseño");
            System.out.println("  [7] Eliminar diseño (DELETE)");
            System.out.println("  [0] Volver al menú principal");
            System.out.println(DIVISOR);

            int opcion = leerEntero("Seleccione operación: ");
            switch (opcion) {
                case 1 -> listarDisenosPorProyecto();
                case 2 -> buscarDisenoPorId();
                case 3 -> listarDisenosPorEstado();
                case 4 -> crearDiseno();
                case 5 -> actualizarDiseno();
                case 6 -> cambiarEstadoDiseno();
                case 7 -> eliminarDiseno();
                case 0 -> enMenu = false;
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static void listarDisenosPorProyecto() {
        int idProyecto = leerEntero("ID del proyecto: ");
        List<Diseno> disenos = disenoDAO.listarPorProyecto(idProyecto);
        if (disenos.isEmpty()) {
            System.out.println("No hay diseños para el proyecto " + idProyecto);
            return;
        }
        disenos.forEach(d -> System.out.println("  " + d));
    }

    private static void buscarDisenoPorId() {
        int id = leerEntero("ID del diseño a buscar: ");
        Diseno diseno = disenoDAO.buscarPorId(id);
        if (diseno != null) {
            System.out.println("✅ Encontrado: " + diseno);
        } else {
            System.out.println("⚠️  Diseño no encontrado.");
        }
    }

    private static void listarDisenosPorEstado() {
        System.out.println("Estados disponibles: borrador | revision | aprobado");
        String estado = leerTexto("Estado a filtrar: ");
        List<Diseno> disenos = disenoDAO.listarPorEstado(estado);
        if (disenos.isEmpty()) {
            System.out.println("No hay diseños en estado '" + estado + "'.");
            return;
        }
        disenos.forEach(d -> System.out.println("  " + d));
    }

    private static void crearDiseno() {
        System.out.println("\n--- CREAR NUEVO DISEÑO ---");
        String nombre     = leerTexto("Nombre del diseño: ");
        System.out.println("Tipos: esquematico | pcb | layout");
        String tipo       = leerTexto("Tipo: ");
        String formato    = leerTexto("Formato/Herramienta EDA (ej. KiCad 7.0): ");
        int    idProyecto = leerEntero("ID del proyecto al que pertenece: ");
        int    idCreador  = leerEntero("ID del usuario creador: ");

        Diseno nuevo = new Diseno(nombre, tipo, formato, "borrador", false, idProyecto, idCreador);
        boolean resultado = disenoDAO.insertarDiseno(nuevo);
        System.out.println(resultado
            ? "✅ Diseño creado con ID: " + nuevo.getIdDiseno()
            : "❌ No se pudo crear el diseño.");
    }

    private static void actualizarDiseno() {
        System.out.println("\n--- ACTUALIZAR DISEÑO ---");
        int id = leerEntero("ID del diseño a actualizar: ");
        Diseno existente = disenoDAO.buscarPorId(id);
        if (existente == null) {
            System.out.println("⚠️  Diseño no encontrado.");
            return;
        }
        System.out.println("Diseño actual: " + existente);

        String nombre  = leerTexto("Nuevo nombre [Enter para mantener]: ");
        String formato = leerTexto("Nuevo formato [Enter para mantener]: ");

        if (!nombre.isBlank())  existente.setNombreDiseno(nombre);
        if (!formato.isBlank()) existente.setFormato(formato);

        boolean resultado = disenoDAO.actualizarDiseno(existente);
        System.out.println(resultado ? "✅ Diseño actualizado." : "❌ No se pudo actualizar.");
    }

    private static void cambiarEstadoDiseno() {
        int id = leerEntero("ID del diseño: ");
        System.out.println("Estados disponibles: borrador | revision | aprobado");
        String nuevoEstado = leerTexto("Nuevo estado: ");
        boolean resultado = disenoDAO.actualizarEstado(id, nuevoEstado);
        System.out.println(resultado ? "✅ Estado actualizado." : "❌ No se pudo actualizar.");
    }

    private static void eliminarDiseno() {
        System.out.println("\n--- ELIMINAR DISEÑO ---");
        int id = leerEntero("ID del diseño a eliminar: ");
        System.out.print("⚠️  ¿Confirma eliminación del diseño ID=" + id + "? (s/n): ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("s")) {
            boolean resultado = disenoDAO.eliminarDiseno(id);
            System.out.println(resultado ? "✅ Diseño eliminado." : "❌ No se pudo eliminar.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MÓDULO 3 — GESTIÓN DE USUARIOS
    // ══════════════════════════════════════════════════════════════

    private static void menuUsuarios() {
        boolean enMenu = true;
        while (enMenu) {
            System.out.println("\n" + DIVISOR);
            System.out.println("  👤 GESTIÓN DE USUARIOS");
            System.out.println(DIVISOR);
            System.out.println("  [1] Listar todos los usuarios");
            System.out.println("  [2] Buscar usuario por ID");
            System.out.println("  [3] Buscar usuario por email");
            System.out.println("  [4] Registrar nuevo usuario (INSERT)");
            System.out.println("  [5] Actualizar usuario (UPDATE)");
            System.out.println("  [6] Cambiar estado de cuenta");
            System.out.println("  [7] Eliminar usuario (DELETE)");
            System.out.println("  [0] Volver al menú principal");
            System.out.println(DIVISOR);

            int opcion = leerEntero("Seleccione operación: ");
            switch (opcion) {
                case 1 -> listarUsuarios();
                case 2 -> buscarUsuarioPorId();
                case 3 -> buscarUsuarioPorEmail();
                case 4 -> registrarUsuario();
                case 5 -> actualizarUsuario();
                case 6 -> cambiarEstadoUsuario();
                case 7 -> eliminarUsuario();
                case 0 -> enMenu = false;
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static void listarUsuarios() {
        System.out.println("\n--- LISTADO DE USUARIOS ---");
        List<Usuario> usuarios = usuarioDAO.listarUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }
        usuarios.forEach(u -> System.out.println("  " + u));
    }

    private static void buscarUsuarioPorId() {
        int id = leerEntero("ID del usuario a buscar: ");
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario != null) {
            System.out.println("✅ Encontrado: " + usuario);
        } else {
            System.out.println("⚠️  Usuario no encontrado.");
        }
    }

    private static void buscarUsuarioPorEmail() {
        String email = leerTexto("Email a buscar: ");
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario != null) {
            System.out.println("✅ Encontrado: " + usuario);
        } else {
            System.out.println("⚠️  No existe un usuario con ese email.");
        }
    }

    private static void registrarUsuario() {
        System.out.println("\n--- REGISTRAR NUEVO USUARIO ---");
        String nombre  = leerTexto("Nombre completo: ");
        String email   = leerTexto("Correo electrónico: ");
        String hash    = leerTexto("Hash de contraseña (SHA-256): ");
        System.out.println("Roles disponibles: disenador | administrador | colaborador");
        String rol     = leerTexto("Rol: ");
        String nivel   = leerTexto("Nivel de acceso: ");

        Usuario nuevo = new Usuario(nombre, email, hash, rol, nivel, "activo");
        boolean resultado = usuarioDAO.insertarUsuario(nuevo);
        System.out.println(resultado
            ? "✅ Usuario registrado con ID: " + nuevo.getIdUsuario()
            : "❌ No se pudo registrar el usuario.");
    }

    private static void actualizarUsuario() {
        System.out.println("\n--- ACTUALIZAR USUARIO ---");
        int id = leerEntero("ID del usuario a actualizar: ");
        Usuario existente = usuarioDAO.buscarPorId(id);
        if (existente == null) {
            System.out.println("⚠️  Usuario no encontrado.");
            return;
        }
        System.out.println("Usuario actual: " + existente);

        String nombre = leerTexto("Nuevo nombre [Enter para mantener]: ");
        String email  = leerTexto("Nuevo email [Enter para mantener]: ");

        if (!nombre.isBlank()) existente.setNombre(nombre);
        if (!email.isBlank())  existente.setEmail(email);

        boolean resultado = usuarioDAO.actualizarUsuario(existente);
        System.out.println(resultado ? "✅ Usuario actualizado." : "❌ No se pudo actualizar.");
    }

    private static void cambiarEstadoUsuario() {
        int id = leerEntero("ID del usuario: ");
        System.out.println("Estados disponibles: activo | inactivo | suspendido");
        String nuevoEstado = leerTexto("Nuevo estado: ");
        boolean resultado = usuarioDAO.actualizarEstado(id, nuevoEstado);
        System.out.println(resultado ? "✅ Estado actualizado." : "❌ No se pudo actualizar.");
    }

    private static void eliminarUsuario() {
        System.out.println("\n--- ELIMINAR USUARIO ---");
        int id = leerEntero("ID del usuario a eliminar: ");
        System.out.print("⚠️  ¿Confirma eliminación del usuario ID=" + id + "? (s/n): ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("s")) {
            boolean resultado = usuarioDAO.eliminarUsuario(id);
            System.out.println(resultado ? "✅ Usuario eliminado." : "❌ No se pudo eliminar (puede tener registros dependientes).");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // DEMOSTRACIÓN CRUD COMPLETA (modo automático para evidencia)
    // ══════════════════════════════════════════════════════════════

    /**
     * Ejecuta una secuencia CRUD completa de forma automática
     * para demostrar las cuatro operaciones en los tres módulos.
     * Diseñada para captura de pantalla como evidencia de la actividad.
     */
    private static void ejecutarDemostracionCompleta() {
        System.out.println("\n" + LINEA);
        System.out.println("  🎯 DEMOSTRACIÓN CRUD COMPLETA — GA7-220501096-AA2-EV01");
        System.out.println(LINEA);

        // ─── PROYECTOS ───────────────────────────────────────────
        System.out.println("\n📁 [PROYECTO] ── SELECT: Listando todos los proyectos...");
        List<Proyecto> proyectos = proyectoDAO.listarProyectos();
        proyectos.forEach(p -> System.out.println("   " + p));

        System.out.println("\n📁 [PROYECTO] ── INSERT: Creando proyecto de prueba...");
        Proyecto nuevoProy = new Proyecto(
            "Tarjeta RF 5G Prototipo",
            "PCB de prototipado para pruebas de antenas 5G sub-6GHz",
            "mixto", "activo", "0.1.0", 1
        );
        proyectoDAO.insertarProyecto(nuevoProy);

        System.out.println("\n📁 [PROYECTO] ── UPDATE: Actualizando versión del proyecto creado...");
        nuevoProy.setVersionActual("0.2.0");
        nuevoProy.setEstado("activo");
        proyectoDAO.actualizarProyecto(nuevoProy);

        System.out.println("\n📁 [PROYECTO] ── SELECT filtrado: Proyectos con estado 'activo'...");
        List<Proyecto> activos = proyectoDAO.buscarPorEstado("activo");
        activos.forEach(p -> System.out.println("   " + p));

        System.out.println("\n📁 [PROYECTO] ── DELETE: Eliminando proyecto de prueba ID=" + nuevoProy.getIdProyecto() + "...");
        proyectoDAO.eliminarProyecto(nuevoProy.getIdProyecto());

        // ─── DISEÑOS ─────────────────────────────────────────────
        System.out.println("\n🔌 [DISEÑO] ── SELECT: Listando diseños del proyecto 1...");
        List<Diseno> disenos = disenoDAO.listarPorProyecto(1);
        disenos.forEach(d -> System.out.println("   " + d));

        System.out.println("\n🔌 [DISEÑO] ── INSERT: Creando diseño de prueba...");
        Diseno nuevoDiseno = new Diseno(
            "Esquemático RF 5G Antena", "esquematico",
            "KiCad 7.0", "borrador", false, 1, 1
        );
        disenoDAO.insertarDiseno(nuevoDiseno);

        System.out.println("\n🔌 [DISEÑO] ── UPDATE: Cambiando estado a 'revision'...");
        disenoDAO.actualizarEstado(nuevoDiseno.getIdDiseno(), "revision");

        System.out.println("\n🔌 [DISEÑO] ── SELECT por estado 'aprobado'...");
        List<Diseno> aprobados = disenoDAO.listarPorEstado("aprobado");
        aprobados.forEach(d -> System.out.println("   " + d));

        System.out.println("\n🔌 [DISEÑO] ── DELETE: Eliminando diseño de prueba ID=" + nuevoDiseno.getIdDiseno() + "...");
        disenoDAO.eliminarDiseno(nuevoDiseno.getIdDiseno());

        // ─── USUARIOS ────────────────────────────────────────────
        System.out.println("\n👤 [USUARIO] ── SELECT: Listando todos los usuarios...");
        List<Usuario> usuarios = usuarioDAO.listarUsuarios();
        usuarios.forEach(u -> System.out.println("   " + u));

        System.out.println("\n👤 [USUARIO] ── INSERT: Registrando usuario de prueba...");
        Usuario nuevoUser = new Usuario(
            "Carlos Martínez Prueba",
            "carlos.prueba@sena.edu.co",
            "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", // SHA-256 de "password"
            "colaborador", "basico", "activo"
        );
        usuarioDAO.insertarUsuario(nuevoUser);

        System.out.println("\n👤 [USUARIO] ── UPDATE: Actualizando nivel de acceso...");
        nuevoUser.setNivelAcceso("intermedio");
        usuarioDAO.actualizarUsuario(nuevoUser);

        System.out.println("\n👤 [USUARIO] ── SELECT por email...");
        Usuario encontrado = usuarioDAO.buscarPorEmail("carlos.prueba@sena.edu.co");
        if (encontrado != null) System.out.println("   " + encontrado);

        System.out.println("\n👤 [USUARIO] ── DELETE: Eliminando usuario de prueba ID=" + nuevoUser.getIdUsuario() + "...");
        usuarioDAO.eliminarUsuario(nuevoUser.getIdUsuario());

        // ─── Resumen ─────────────────────────────────────────────
        System.out.println("\n" + LINEA);
        System.out.println("  ✅ DEMOSTRACIÓN COMPLETADA");
        System.out.println("  Operaciones ejecutadas: INSERT, SELECT, UPDATE, DELETE");
        System.out.println("  Módulos probados: Proyectos, Diseños, Usuarios");
        System.out.println("  Conexión: JDBC → MySQL 8.x → sistema_diseno_circuitos");
        System.out.println(LINEA);
    }

    // ══════════════════════════════════════════════════════════════
    // Utilidades de entrada de consola
    // ══════════════════════════════════════════════════════════════

    /**
     * Lee un número entero desde la consola con manejo de errores.
     * @param mensaje Prompt a mostrar al usuario
     * @return Entero ingresado o -1 si la entrada no es válida
     */
    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        try {
            String linea = scanner.nextLine().trim();
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            System.out.println("⚠️  Ingrese un número válido.");
            return -1;
        }
    }

    /**
     * Lee una línea de texto desde la consola.
     * @param mensaje Prompt a mostrar al usuario
     * @return Texto ingresado (puede estar vacío si el usuario presiona Enter)
     */
    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }
}
