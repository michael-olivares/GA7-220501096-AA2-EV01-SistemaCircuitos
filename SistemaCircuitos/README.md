# ⚡ Sistema de Diseño Integrado de Circuitos
## GA7-220501096-AA2-EV01 — Codificación de Módulos del Software con JDBC

**Autor:** Michael Ronald Olivares Giraldo  
**SENA — Regional Distrito Capital — Centro Metalmecánico**  
**Ficha:** 3118306 — Tecnología en Análisis y Desarrollo de Software  
**Fase:** 3 — Ejecución | **Instructor:** Ing. Leonardo Moreno Collazos  
**Fecha:** Abril 2026

---

## 📋 Descripción

Módulo Java que implementa las operaciones CRUD (Insertar, Consultar, Actualizar, Eliminar) 
sobre la base de datos `sistema_diseno_circuitos` mediante **JDBC** (Java Database Connectivity), 
conectado a **MySQL 8.x**.

El proyecto se basa en los artefactos del ciclo de software desarrollados en evidencias anteriores:
- **BD:** `GA6-220501096-AA2-EV03` — Script MySQL con 21 tablas, 5 vistas, 4 procedures, 3 triggers
- **Interfaces:** `GA6-220501096-AA3-EV03` — Prototipos HTML/CSS/JS
- **Estándares:** `GA7-220501096-AA1-EV02` — Convenciones de codificación (PascalCase, camelCase, etc.)

---

## 🏗️ Estructura del Proyecto

```
SistemaCircuitos/
├── src/main/java/co/sena/adso/circuitos/
│   ├── conexion/
│   │   └── ConexionBD.java          ← Singleton JDBC — gestión de conexión MySQL
│   ├── modelo/
│   │   ├── Proyecto.java            ← Entidad PROYECTO (tabla proyecto)
│   │   ├── Diseno.java              ← Entidad DISEÑO (tabla diseno)
│   │   └── Usuario.java             ← Entidad USUARIO (tabla usuario)
│   ├── dao/
│   │   ├── ProyectoDAO.java         ← CRUD completo: proyecto
│   │   ├── DisenoDAO.java           ← CRUD completo: diseno
│   │   └── UsuarioDAO.java          ← CRUD completo: usuario
│   └── ui/
│       └── MenuPrincipal.java       ← Punto de entrada — menú de consola
├── lib/
│   └── mysql-connector-j-8.x.jar   ← Driver JDBC MySQL (agregar manualmente)
├── SISTEMA_DISENO_CIRCUITOS_MODELO_FISICO_COMPLETO.sql
└── README.md
```

---

## ⚙️ Requisitos

| Herramienta | Versión mínima |
|---|---|
| Java JDK | 17 o superior |
| MySQL Server | 8.0 o superior |
| MySQL Connector/J | 8.0.x |
| IDE sugerido | IntelliJ IDEA / Eclipse / VS Code |

---

## 🚀 Configuración y Ejecución

### 1. Preparar la Base de Datos

```sql
-- En MySQL Workbench o MySQL Shell:
source SISTEMA_DISENO_CIRCUITOS_MODELO_FISICO_COMPLETO.sql;
```

Esto crea la base de datos `sistema_diseno_circuitos` con las 21 tablas y los ~95 registros de prueba.

### 2. Configurar la Conexión JDBC

Editar `ConexionBD.java` si sus credenciales MySQL son diferentes:

```java
private static final String URL     = "jdbc:mysql://localhost:3306/sistema_diseno_circuitos"
                                    + "?useSSL=false&serverTimezone=America/Bogota";
private static final String USUARIO = "root";      // ← su usuario MySQL
private static final String CLAVE   = "root";      // ← su contraseña MySQL
```

### 3. Agregar el Driver JDBC

Descargar **MySQL Connector/J** desde:  
👉 https://dev.mysql.com/downloads/connector/j/

Agregar el archivo `.jar` a la carpeta `lib/` y configurarlo como dependencia en el IDE.

**En IntelliJ IDEA:**  
`File > Project Structure > Modules > Dependencies > + > JARs or directories`

### 4. Ejecutar la Aplicación

Ejecutar la clase principal:
```
co.sena.adso.circuitos.ui.MenuPrincipal
```

O desde terminal (con el JAR en el classpath):
```bash
java -cp ".:lib/mysql-connector-j-8.x.jar" co.sena.adso.circuitos.ui.MenuPrincipal
```

---

## 🗄️ Módulos CRUD Implementados

### 📁 Módulo 1: Proyectos (`ProyectoDAO`)
| Operación | Método | SQL |
|---|---|---|
| CREATE | `insertarProyecto(Proyecto)` | `INSERT INTO proyecto ...` |
| READ | `listarProyectos()` | `SELECT ... FROM proyecto ORDER BY fecha_creacion DESC` |
| READ | `buscarPorId(int)` | `SELECT ... WHERE id_proyecto = ?` |
| READ | `buscarPorEstado(String)` | `SELECT ... WHERE estado = ?` |
| UPDATE | `actualizarProyecto(Proyecto)` | `UPDATE proyecto SET ... WHERE id_proyecto = ?` |
| DELETE | `eliminarProyecto(int)` | `DELETE FROM proyecto WHERE id_proyecto = ?` |

### 🔌 Módulo 2: Diseños (`DisenoDAO`)
| Operación | Método | SQL |
|---|---|---|
| CREATE | `insertarDiseno(Diseno)` | `INSERT INTO diseno ...` |
| READ | `listarPorProyecto(int)` | `SELECT ... WHERE id_proyecto = ?` |
| READ | `buscarPorId(int)` | `SELECT ... WHERE id_diseno = ?` |
| READ | `listarPorEstado(String)` | `SELECT ... WHERE estado = ?` |
| UPDATE | `actualizarDiseno(Diseno)` | `UPDATE diseno SET ... WHERE id_diseno = ?` |
| UPDATE | `actualizarEstado(int, String)` | `UPDATE diseno SET estado = ? WHERE id_diseno = ?` |
| DELETE | `eliminarDiseno(int)` | `DELETE FROM diseno WHERE id_diseno = ?` |

### 👤 Módulo 3: Usuarios (`UsuarioDAO`)
| Operación | Método | SQL |
|---|---|---|
| CREATE | `insertarUsuario(Usuario)` | `INSERT INTO usuario ...` |
| READ | `listarUsuarios()` | `SELECT ... FROM usuario ORDER BY nombre` |
| READ | `buscarPorId(int)` | `SELECT ... WHERE id_usuario = ?` |
| READ | `buscarPorEmail(String)` | `SELECT ... WHERE email = ?` |
| UPDATE | `actualizarUsuario(Usuario)` | `UPDATE usuario SET ... WHERE id_usuario = ?` |
| UPDATE | `actualizarEstado(int, String)` | `UPDATE usuario SET estado = ? WHERE id_usuario = ?` |
| DELETE | `eliminarUsuario(int)` | `DELETE FROM usuario WHERE id_usuario = ?` |

---

## 📐 Estándares de Codificación Aplicados

Conforme a `GA7-220501096-AA1-EV02`:

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | `co.sena.adso.circuitos.*` | dominio invertido SENA |
| Clases | PascalCase | `ProyectoDAO`, `ConexionBD` |
| Métodos | camelCase | `insertarProyecto()`, `buscarPorId()` |
| Variables | camelCase | `idProyecto`, `gestorConexion` |
| Constantes | UPPER_SNAKE_CASE | `SQL_INSERTAR`, `LINEA` |
| Comentarios | Javadoc en métodos públicos | `@param`, `@return` |
| SQL | Constantes privadas de clase | `private static final String SQL_*` |

---

## 🔗 Repositorio Git

```
https://github.com/USUARIO/GA7-220501096-AA2-EV01-SistemaCircuitos
```

El proyecto usa **Git Flow** con ramas:
- `main` — código estable y revisado
- `develop` — integración continua  
- `feature/crud-proyecto` — desarrollo de módulo proyecto
- `feature/crud-diseno` — desarrollo de módulo diseño
- `feature/crud-usuario` — desarrollo de módulo usuario

Commits con **Conventional Commits**:
```
feat(proyecto): implementar CRUD completo con PreparedStatement
feat(diseno): agregar DAO con operaciones INSERT SELECT UPDATE DELETE
feat(usuario): implementar buscarPorEmail para autenticación
fix(conexion): corregir cierre de PreparedStatement en cada operación
docs(readme): agregar instrucciones de configuración y ejecución
```

---

## 🎓 Referencias

- SENA. (2026). GA6-220501096-AA2-EV03 — Script BD Sistema de Diseño Integrado de Circuitos.
- SENA. (2026). GA7-220501096-AA1-EV02 — Estándares de Codificación. Olivares Giraldo, M. R.
- Oracle. (2024). JDBC API Documentation. https://docs.oracle.com/javase/tutorial/jdbc/
- MySQL. (2024). MySQL Connector/J Developer Guide. https://dev.mysql.com/doc/connector-j/
- Horstmann, C. (2019). *Core Java, Volume I — Fundamentals* (11th ed.). Prentice Hall.
