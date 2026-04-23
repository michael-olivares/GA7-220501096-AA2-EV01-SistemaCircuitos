-- ============================================================
-- SISTEMA DE DISEÑO INTEGRADO DE CIRCUITOS
-- GA6-220501096-AA2-EV03 - SCRIPT DE BASE DE DATOS DEL PROYECTO
-- Michael Ronald Olivares Giraldo
-- SENA - Ficha 3118306 - Febrero 2026
-- MySQL Workbench - Modelo Físico Completo
-- ============================================================

-- ============================================================
-- SECCIÓN 1: CREACIÓN DE LA BASE DE DATOS
-- ============================================================

DROP DATABASE IF EXISTS sistema_diseno_circuitos;

CREATE DATABASE sistema_diseno_circuitos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_diseno_circuitos;

-- ============================================================
-- SECCIÓN 2: DDL - CREACIÓN DE TABLAS CON RESTRICCIONES
-- ============================================================

-- DOMINIO 1: NÚCLEO DEL SISTEMA
-- -------------------------------------------------------

-- Tabla 1: USUARIO
CREATE TABLE usuario (
    id_usuario            INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(100)    NOT NULL,
    email                 VARCHAR(100)    NOT NULL UNIQUE,
    contrasena_hash       VARCHAR(255)    NOT NULL,
    rol                   ENUM('disenador','administrador','colaborador') NOT NULL,
    nivel_acceso          VARCHAR(50)     NOT NULL,
    fecha_registro        DATETIME        DEFAULT CURRENT_TIMESTAMP,
    preferencias_interfaz JSON,
    estado                ENUM('activo','inactivo','suspendido') DEFAULT 'activo',
    CONSTRAINT chk_email CHECK (email LIKE '%@%.%')
) ENGINE=InnoDB COMMENT='Actores del sistema: diseñadores, administradores y colaboradores';

-- Tabla 2: PROYECTO
CREATE TABLE proyecto (
    id_proyecto             INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                  VARCHAR(200)    NOT NULL,
    descripcion             TEXT,
    tipo_circuito           ENUM('mixto','microcontrolador','asic','fpga','analogico') NOT NULL,
    estado                  ENUM('activo','finalizado','archivado') DEFAULT 'activo',
    fecha_creacion          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion      DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    version_actual          VARCHAR(20),
    configuracion_seguridad JSON,
    id_usuario_creador      INT             NOT NULL,
    CONSTRAINT fk_proyecto_usuario
        FOREIGN KEY (id_usuario_creador) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_proyecto_usuario (id_usuario_creador),
    INDEX idx_proyecto_estado  (estado)
) ENGINE=InnoDB COMMENT='Unidad organizacional que agrupa diseños de circuitos';

-- Tabla 3: DISEÑO
CREATE TABLE diseno (
    id_diseno             INT             PRIMARY KEY AUTO_INCREMENT,
    nombre_diseno         VARCHAR(200)    NOT NULL,
    tipo                  ENUM('esquematico','pcb','layout') NOT NULL,
    archivo_diseno        LONGBLOB,
    formato               VARCHAR(50),
    estado                ENUM('borrador','revision','aprobado') DEFAULT 'borrador',
    checksum              VARCHAR(64),
    cifrado               BOOLEAN         DEFAULT FALSE,
    fecha_creacion        DATETIME        DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion    DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    id_proyecto           INT             NOT NULL,
    id_usuario_creador    INT             NOT NULL,
    CONSTRAINT fk_diseno_proyecto
        FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diseno_usuario
        FOREIGN KEY (id_usuario_creador) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_diseno_proyecto (id_proyecto),
    INDEX idx_diseno_estado   (estado)
) ENGINE=InnoDB COMMENT='Archivos de circuitos electrónicos: esquemáticos, PCB y layout';

-- Tabla 4: VERSION
CREATE TABLE version (
    id_version            INT             PRIMARY KEY AUTO_INCREMENT,
    numero_version        VARCHAR(20)     NOT NULL,
    descripcion_cambios   TEXT,
    fecha_creacion        DATETIME        DEFAULT CURRENT_TIMESTAMP,
    autor                 VARCHAR(100)    NOT NULL,
    tag                   VARCHAR(50),
    es_release            BOOLEAN         DEFAULT FALSE,
    id_diseno             INT             NOT NULL,
    CONSTRAINT fk_version_diseno
        FOREIGN KEY (id_diseno) REFERENCES diseno(id_diseno)
        ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_version_diseno (id_diseno, numero_version),
    INDEX idx_version_diseno (id_diseno)
) ENGINE=InnoDB COMMENT='Historial de versiones de cada diseño con numeración semántica';

-- DOMINIO 2: RECURSOS Y COMPONENTES
-- -------------------------------------------------------

-- Tabla 5: BIBLIOTECA
CREATE TABLE biblioteca (
    id_biblioteca         INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(200)    NOT NULL,
    tipo                  ENUM('simbolos','footprints','modelos','mixta') NOT NULL,
    descripcion           TEXT,
    version               VARCHAR(20),
    es_personalizada      BOOLEAN         DEFAULT FALSE,
    fecha_actualizacion   DATETIME        ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Repositorios de símbolos, footprints y modelos de simulación';

-- Tabla 6: COMPONENTE
CREATE TABLE componente (
    id_componente         INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(200)    NOT NULL,
    categoria             VARCHAR(100)    NOT NULL,
    fabricante            VARCHAR(100)    NOT NULL,
    modelo                VARCHAR(100)    NOT NULL,
    especificaciones      JSON,
    precio                DECIMAL(10,2),
    disponibilidad        BOOLEAN         DEFAULT TRUE,
    footprint             VARCHAR(100),
    id_biblioteca         INT,
    fecha_actualizacion   DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comp_fab_mod (fabricante, modelo),
    CONSTRAINT fk_componente_biblioteca
        FOREIGN KEY (id_biblioteca) REFERENCES biblioteca(id_biblioteca)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_precio CHECK (precio IS NULL OR precio >= 0),
    INDEX idx_componente_categoria      (categoria),
    INDEX idx_componente_disponibilidad (disponibilidad)
) ENGINE=InnoDB COMMENT='Catálogo de elementos electrónicos disponibles para diseños';

-- Tabla 7: PROVEEDOR
CREATE TABLE proveedor (
    id_proveedor          INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(200)    NOT NULL,
    contacto              VARCHAR(200),
    calificacion          DECIMAL(3,2),
    tiempo_entrega        INT             COMMENT 'Días hábiles de entrega',
    terminos_comerciales  TEXT,
    CONSTRAINT chk_calificacion CHECK (calificacion BETWEEN 0 AND 5),
    CONSTRAINT chk_tiempo_entrega CHECK (tiempo_entrega IS NULL OR tiempo_entrega > 0)
) ENGINE=InnoDB COMMENT='Suministradores de componentes electrónicos';

-- Tabla 8: DISEÑO_COMPONENTE (Relación N:M)
CREATE TABLE diseno_componente (
    id_diseno             INT             NOT NULL,
    id_componente         INT             NOT NULL,
    cantidad              INT             DEFAULT 1,
    posicion_x            DECIMAL(10,3),
    posicion_y            DECIMAL(10,3),
    rotation              DECIMAL(5,2)    DEFAULT 0.0,
    referencia            VARCHAR(20),
    PRIMARY KEY (id_diseno, id_componente),
    CONSTRAINT fk_dc_diseno
        FOREIGN KEY (id_diseno) REFERENCES diseno(id_diseno)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_dc_componente
        FOREIGN KEY (id_componente) REFERENCES componente(id_componente)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Relación N:M entre diseños y componentes electrónicos';

-- Tabla 9: COMPONENTE_PROVEEDOR (Relación N:M)
CREATE TABLE componente_proveedor (
    id_componente         INT             NOT NULL,
    id_proveedor          INT             NOT NULL,
    precio_proveedor      DECIMAL(10,2),
    tiempo_entrega        INT             COMMENT 'Días hábiles',
    PRIMARY KEY (id_componente, id_proveedor),
    CONSTRAINT fk_cp_componente
        FOREIGN KEY (id_componente) REFERENCES componente(id_componente)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cp_proveedor
        FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_precio_prov CHECK (precio_proveedor IS NULL OR precio_proveedor >= 0)
) ENGINE=InnoDB COMMENT='Relación N:M entre componentes y proveedores con precios';

-- DOMINIO 3: PROCESOS Y VALIDACIÓN
-- -------------------------------------------------------

-- Tabla 10: SIMULACION
CREATE TABLE simulacion (
    id_simulacion             INT             PRIMARY KEY AUTO_INCREMENT,
    tipo                      ENUM('analogica','digital','mixta','termica','energetica') NOT NULL,
    parametros                JSON,
    condiciones_ambientales   JSON,
    fecha_ejecucion           DATETIME        DEFAULT CURRENT_TIMESTAMP,
    duracion                  INT             COMMENT 'Duración en segundos',
    estado                    ENUM('pendiente','ejecutando','completada','error') DEFAULT 'pendiente',
    id_diseno                 INT             NOT NULL,
    id_usuario                INT             NOT NULL,
    CONSTRAINT fk_sim_diseno
        FOREIGN KEY (id_diseno) REFERENCES diseno(id_diseno)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sim_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_simulacion_diseno  (id_diseno),
    INDEX idx_simulacion_estado  (estado)
) ENGINE=InnoDB COMMENT='Procesos de análisis virtual: analógica, digital, mixta, térmica y energética';

-- Tabla 11: RESULTADO_SIMULACION (Relación 1:1)
CREATE TABLE resultado_simulacion (
    id_resultado              INT             PRIMARY KEY AUTO_INCREMENT,
    datos_salida              LONGBLOB,
    graficos                  LONGBLOB,
    metricas                  JSON,
    alertas                   TEXT,
    cumple_especificaciones   BOOLEAN,
    fecha_generacion          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    id_simulacion             INT             UNIQUE NOT NULL,
    CONSTRAINT fk_resultado_sim
        FOREIGN KEY (id_simulacion) REFERENCES simulacion(id_simulacion)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Datos de salida de simulaciones (relación 1:1 con simulacion)';

-- Tabla 12: PRUEBA
CREATE TABLE prueba (
    id_prueba             INT             PRIMARY KEY AUTO_INCREMENT,
    tipo                  ENUM('funcional','consumo','rendimiento','temperatura','emc','stress') NOT NULL,
    criterios             JSON,
    parametros            JSON,
    fecha_ejecucion       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    resultado             ENUM('exitosa','fallida','pendiente') DEFAULT 'pendiente',
    id_diseno             INT             NOT NULL,
    CONSTRAINT fk_prueba_diseno
        FOREIGN KEY (id_diseno) REFERENCES diseno(id_diseno)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_prueba_diseno    (id_diseno),
    INDEX idx_prueba_resultado (resultado)
) ENGINE=InnoDB COMMENT='Procesos de validación automatizada de diseños';

-- Tabla 13: INFORME_PRUEBA (Relación 1:1)
CREATE TABLE informe_prueba (
    id_informe            INT             PRIMARY KEY AUTO_INCREMENT,
    formato               ENUM('pdf','html','xml') NOT NULL,
    contenido             LONGBLOB,
    analisis_comparativo  JSON,
    recomendaciones       TEXT,
    fecha_generacion      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    id_prueba             INT             UNIQUE NOT NULL,
    CONSTRAINT fk_informe_prueba
        FOREIGN KEY (id_prueba) REFERENCES prueba(id_prueba)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Documentación de resultados de prueba (relación 1:1 con prueba)';

-- Tabla 14: FIRMWARE
CREATE TABLE firmware (
    id_firmware           INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(200)    NOT NULL,
    version               VARCHAR(20)     NOT NULL,
    arquitectura          VARCHAR(50),
    codigo_fuente         LONGBLOB,
    binario_compilado     LONGBLOB,
    fecha_compilacion     DATETIME,
    id_diseno             INT             NOT NULL,
    CONSTRAINT fk_firmware_diseno
        FOREIGN KEY (id_diseno) REFERENCES diseno(id_diseno)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_firmware_diseno (id_diseno)
) ENGINE=InnoDB COMMENT='Código embebido para microcontroladores asociado a diseños';

-- DOMINIO 4: SOPORTE Y ADMINISTRACIÓN
-- -------------------------------------------------------

-- Tabla 15: ESTANDAR
CREATE TABLE estandar (
    id_estandar           INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(200)    NOT NULL,
    tipo                  VARCHAR(100)    NOT NULL,
    version               VARCHAR(20),
    requisitos            TEXT,
    industria             VARCHAR(100)
) ENGINE=InnoDB COMMENT='Normativas de cumplimiento industrial: ISO, IEC, FDA, etc.';

-- Tabla 16: PROYECTO_ESTANDAR (Relación N:M)
CREATE TABLE proyecto_estandar (
    id_proyecto           INT             NOT NULL,
    id_estandar           INT             NOT NULL,
    PRIMARY KEY (id_proyecto, id_estandar),
    CONSTRAINT fk_pe_proyecto
        FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pe_estandar
        FOREIGN KEY (id_estandar) REFERENCES estandar(id_estandar)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Relación N:M entre proyectos y estándares de cumplimiento';

-- Tabla 17: SISTEMA_EXTERNO
CREATE TABLE sistema_externo (
    id_sistema            INT             PRIMARY KEY AUTO_INCREMENT,
    nombre                VARCHAR(200)    NOT NULL,
    tipo                  VARCHAR(100)    NOT NULL,
    url_api               VARCHAR(500),
    configuracion         JSON,
    estado_conexion       ENUM('activo','inactivo','error') DEFAULT 'inactivo'
) ENGINE=InnoDB COMMENT='Sistemas externos integrados: PLM, ERP y herramientas CAD';

-- Tabla 18: PROYECTO_SISTEMA_EXTERNO (Relación N:M)
CREATE TABLE proyecto_sistema_externo (
    id_proyecto           INT             NOT NULL,
    id_sistema            INT             NOT NULL,
    PRIMARY KEY (id_proyecto, id_sistema),
    CONSTRAINT fk_pse_proyecto
        FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pse_sistema
        FOREIGN KEY (id_sistema) REFERENCES sistema_externo(id_sistema)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Relación N:M entre proyectos y sistemas externos';

-- Tabla 19: DOCUMENTACION
CREATE TABLE documentacion (
    id_documento          INT             PRIMARY KEY AUTO_INCREMENT,
    tipo                  VARCHAR(100)    NOT NULL,
    formato               ENUM('pdf','html','docx','xml') NOT NULL,
    contenido             LONGBLOB,
    nivel_detalle         VARCHAR(50),
    fecha_generacion      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    plantilla_utilizada   VARCHAR(100),
    id_proyecto           INT             NOT NULL,
    CONSTRAINT fk_doc_proyecto
        FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_doc_proyecto (id_proyecto)
) ENGINE=InnoDB COMMENT='Manuales técnicos y reportes generados para cada proyecto';

-- Tabla 20: AUDITORIA
CREATE TABLE auditoria (
    id_auditoria          INT             PRIMARY KEY AUTO_INCREMENT,
    tabla_afectada        VARCHAR(100)    NOT NULL,
    operacion             ENUM('INSERT','UPDATE','DELETE','SELECT') NOT NULL,
    id_registro           INT,
    valores_anteriores    JSON,
    valores_nuevos        JSON,
    id_usuario            INT,
    timestamp             DATETIME        DEFAULT CURRENT_TIMESTAMP,
    direccion_ip          VARCHAR(45),
    nivel_criticidad      ENUM('bajo','medio','alto','critico') NOT NULL,
    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_auditoria_usuario   (id_usuario),
    INDEX idx_auditoria_tabla     (tabla_afectada),
    INDEX idx_auditoria_timestamp (timestamp)
) ENGINE=InnoDB COMMENT='Registro de operaciones críticas para trazabilidad y cumplimiento';

-- Tabla 21: CONFIGURACION_USUARIO (Relación 1:1)
CREATE TABLE configuracion_usuario (
    id_config             INT             PRIMARY KEY AUTO_INCREMENT,
    tipo_interfaz         VARCHAR(50),
    panel_personalizado   JSON,
    notificaciones        JSON,
    id_usuario            INT             UNIQUE NOT NULL,
    CONSTRAINT fk_config_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Preferencias individuales de interfaz por usuario (1:1 con usuario)';

-- ============================================================
-- SECCIÓN 3: VISTAS
-- ============================================================

-- Vista 1: Proyectos activos con información del creador
CREATE OR REPLACE VIEW v_proyectos_activos AS
    SELECT
        p.id_proyecto,
        p.nombre                    AS nombre_proyecto,
        p.tipo_circuito,
        p.estado,
        p.fecha_creacion,
        p.version_actual,
        u.nombre                    AS creador,
        u.email                     AS email_creador,
        u.rol                       AS rol_creador
    FROM proyecto p
    INNER JOIN usuario u ON p.id_usuario_creador = u.id_usuario
    WHERE p.estado = 'activo';

-- Vista 2: Resumen de diseños por proyecto
CREATE OR REPLACE VIEW v_resumen_disenos AS
    SELECT
        p.id_proyecto,
        p.nombre                    AS proyecto,
        COUNT(d.id_diseno)          AS total_disenos,
        SUM(CASE WHEN d.estado = 'borrador'  THEN 1 ELSE 0 END) AS en_borrador,
        SUM(CASE WHEN d.estado = 'revision'  THEN 1 ELSE 0 END) AS en_revision,
        SUM(CASE WHEN d.estado = 'aprobado'  THEN 1 ELSE 0 END) AS aprobados
    FROM proyecto p
    LEFT JOIN diseno d ON p.id_proyecto = d.id_proyecto
    GROUP BY p.id_proyecto, p.nombre;

-- Vista 3: Componentes disponibles con su biblioteca
CREATE OR REPLACE VIEW v_componentes_disponibles AS
    SELECT
        c.id_componente,
        c.nombre                    AS componente,
        c.categoria,
        c.fabricante,
        c.modelo,
        c.precio,
        b.nombre                    AS biblioteca,
        b.tipo                      AS tipo_biblioteca
    FROM componente c
    LEFT JOIN biblioteca b ON c.id_biblioteca = b.id_biblioteca
    WHERE c.disponibilidad = TRUE;

-- Vista 4: Historial de simulaciones con estado
CREATE OR REPLACE VIEW v_historial_simulaciones AS
    SELECT
        s.id_simulacion,
        s.tipo                      AS tipo_simulacion,
        s.estado,
        s.fecha_ejecucion,
        s.duracion,
        d.nombre_diseno             AS diseno,
        p.nombre                    AS proyecto,
        u.nombre                    AS ejecutado_por,
        rs.cumple_especificaciones
    FROM simulacion s
    INNER JOIN diseno d   ON s.id_diseno  = d.id_diseno
    INNER JOIN proyecto p ON d.id_proyecto = p.id_proyecto
    INNER JOIN usuario u  ON s.id_usuario  = u.id_usuario
    LEFT  JOIN resultado_simulacion rs ON s.id_simulacion = rs.id_simulacion;

-- Vista 5: Auditoría de últimas operaciones críticas
CREATE OR REPLACE VIEW v_auditoria_critica AS
    SELECT
        a.id_auditoria,
        a.tabla_afectada,
        a.operacion,
        a.nivel_criticidad,
        a.timestamp,
        a.direccion_ip,
        u.nombre                    AS usuario,
        u.rol
    FROM auditoria a
    LEFT JOIN usuario u ON a.id_usuario = u.id_usuario
    WHERE a.nivel_criticidad IN ('alto','critico')
    ORDER BY a.timestamp DESC;

-- ============================================================
-- SECCIÓN 4: PROCEDIMIENTOS ALMACENADOS
-- ============================================================

DELIMITER $$

-- Procedimiento 1: Registrar nuevo usuario con configuración inicial
CREATE PROCEDURE sp_crear_usuario(
    IN  p_nombre    VARCHAR(100),
    IN  p_email     VARCHAR(100),
    IN  p_hash      VARCHAR(255),
    IN  p_rol       ENUM('disenador','administrador','colaborador'),
    IN  p_nivel     VARCHAR(50),
    OUT p_id        INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    INSERT INTO usuario (nombre, email, contrasena_hash, rol, nivel_acceso)
    VALUES (p_nombre, p_email, p_hash, p_rol, p_nivel);

    SET p_id = LAST_INSERT_ID();

    INSERT INTO configuracion_usuario (tipo_interfaz, id_usuario)
    VALUES ('estandar', p_id);

    COMMIT;
END$$

-- Procedimiento 2: Crear proyecto y registrar en auditoría
CREATE PROCEDURE sp_crear_proyecto(
    IN  p_nombre          VARCHAR(200),
    IN  p_descripcion     TEXT,
    IN  p_tipo_circuito   ENUM('mixto','microcontrolador','asic','fpga','analogico'),
    IN  p_id_usuario      INT,
    IN  p_ip              VARCHAR(45),
    OUT p_id_proyecto     INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    INSERT INTO proyecto (nombre, descripcion, tipo_circuito, id_usuario_creador)
    VALUES (p_nombre, p_descripcion, p_tipo_circuito, p_id_usuario);

    SET p_id_proyecto = LAST_INSERT_ID();

    INSERT INTO auditoria (tabla_afectada, operacion, id_registro,
                           valores_nuevos, id_usuario, direccion_ip, nivel_criticidad)
    VALUES ('proyecto', 'INSERT', p_id_proyecto,
            JSON_OBJECT('nombre', p_nombre, 'tipo_circuito', p_tipo_circuito),
            p_id_usuario, p_ip, 'medio');

    COMMIT;
END$$

-- Procedimiento 3: Cambiar estado de un diseño con auditoría
CREATE PROCEDURE sp_cambiar_estado_diseno(
    IN p_id_diseno    INT,
    IN p_nuevo_estado ENUM('borrador','revision','aprobado'),
    IN p_id_usuario   INT,
    IN p_ip           VARCHAR(45)
)
BEGIN
    DECLARE v_estado_anterior VARCHAR(20);

    SELECT estado INTO v_estado_anterior
    FROM diseno WHERE id_diseno = p_id_diseno;

    UPDATE diseno SET estado = p_nuevo_estado
    WHERE id_diseno = p_id_diseno;

    INSERT INTO auditoria (tabla_afectada, operacion, id_registro,
                           valores_anteriores, valores_nuevos,
                           id_usuario, direccion_ip, nivel_criticidad)
    VALUES ('diseno', 'UPDATE', p_id_diseno,
            JSON_OBJECT('estado', v_estado_anterior),
            JSON_OBJECT('estado', p_nuevo_estado),
            p_id_usuario, p_ip, 'alto');
END$$

-- Procedimiento 4: Obtener resumen estadístico de un proyecto
CREATE PROCEDURE sp_resumen_proyecto(IN p_id_proyecto INT)
BEGIN
    SELECT
        p.nombre                            AS proyecto,
        p.tipo_circuito,
        p.estado,
        COUNT(DISTINCT d.id_diseno)         AS total_disenos,
        COUNT(DISTINCT v.id_version)        AS total_versiones,
        COUNT(DISTINCT s.id_simulacion)     AS total_simulaciones,
        COUNT(DISTINCT pr.id_prueba)        AS total_pruebas,
        COUNT(DISTINCT dc.id_componente)    AS componentes_utilizados
    FROM proyecto p
    LEFT JOIN diseno d               ON p.id_proyecto    = d.id_proyecto
    LEFT JOIN version v              ON d.id_diseno       = v.id_diseno
    LEFT JOIN simulacion s           ON d.id_diseno       = s.id_diseno
    LEFT JOIN prueba pr              ON d.id_diseno       = pr.id_diseno
    LEFT JOIN diseno_componente dc   ON d.id_diseno       = dc.id_diseno
    WHERE p.id_proyecto = p_id_proyecto
    GROUP BY p.nombre, p.tipo_circuito, p.estado;
END$$

DELIMITER ;

-- ============================================================
-- SECCIÓN 5: TRIGGERS
-- ============================================================

DELIMITER $$

-- Trigger 1: Actualizar version_actual en proyecto al crear nueva versión
CREATE TRIGGER trg_actualizar_version_proyecto
AFTER INSERT ON version
FOR EACH ROW
BEGIN
    DECLARE v_id_proyecto INT;

    SELECT id_proyecto INTO v_id_proyecto
    FROM diseno WHERE id_diseno = NEW.id_diseno;

    UPDATE proyecto
    SET version_actual = NEW.numero_version
    WHERE id_proyecto = v_id_proyecto
      AND (version_actual IS NULL OR NEW.es_release = TRUE);
END$$

-- Trigger 2: Registrar en auditoría al eliminar un proyecto
CREATE TRIGGER trg_auditoria_delete_proyecto
BEFORE DELETE ON proyecto
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (tabla_afectada, operacion, id_registro,
                           valores_anteriores, nivel_criticidad)
    VALUES ('proyecto', 'DELETE', OLD.id_proyecto,
            JSON_OBJECT(
                'nombre',         OLD.nombre,
                'tipo_circuito',  OLD.tipo_circuito,
                'estado',         OLD.estado
            ),
            'critico');
END$$

-- Trigger 3: Prevenir suspensión del último administrador activo
CREATE TRIGGER trg_proteger_administrador
BEFORE UPDATE ON usuario
FOR EACH ROW
BEGIN
    DECLARE v_admin_count INT;

    IF OLD.rol = 'administrador' AND NEW.estado != 'activo' THEN
        SELECT COUNT(*) INTO v_admin_count
        FROM usuario
        WHERE rol = 'administrador' AND estado = 'activo' AND id_usuario != OLD.id_usuario;

        IF v_admin_count = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'No se puede desactivar el último administrador activo del sistema.';
        END IF;
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- SECCIÓN 6: DML - DATOS DE PRUEBA
-- ============================================================

-- Usuarios del sistema
INSERT INTO usuario (nombre, email, contrasena_hash, rol, nivel_acceso, estado) VALUES
('Michael Olivares',    'michael.olivares@sena.edu.co',  SHA2('Admin2026!', 256),  'administrador', 'total',     'activo'),
('Laura Gómez',         'laura.gomez@circuitos.co',      SHA2('Disenio01', 256),   'disenador',     'escritura', 'activo'),
('Andrés Vargas',       'andres.vargas@circuitos.co',    SHA2('Colab2026',  256),  'colaborador',   'lectura',   'activo'),
('Daniela Torres',     'daniela.torres@circuitos.co',   SHA2('Disenio02', 256),   'disenador',     'escritura', 'activo');

-- Configuración inicial de usuarios
INSERT INTO configuracion_usuario (tipo_interfaz, notificaciones, id_usuario) VALUES
('oscuro',    '{"email": true, "push": true}',   1),
('claro',     '{"email": true, "push": false}',  2),
('estandar',  '{"email": false, "push": false}', 3),
('oscuro',    '{"email": true, "push": true}',   4);

-- Bibliotecas de componentes
INSERT INTO biblioteca (nombre, tipo, descripcion, version, es_personalizada) VALUES
('KiCad Standard Library',    'mixta',       'Biblioteca estándar de KiCad con símbolos y footprints',    '7.0',   FALSE),
('SENA Electronics Library',  'mixta',       'Biblioteca personalizada del programa ADSO',                '1.2',   TRUE),
('Analog Devices Models',     'modelos',     'Modelos SPICE de Analog Devices',                          '2024',  FALSE),
('SMD Footprints IPC-7351',   'footprints',  'Footprints SMD estándar IPC-7351B',                         '2.1',  FALSE);

-- Componentes electrónicos
INSERT INTO componente (nombre, categoria, fabricante, modelo, especificaciones, precio, disponibilidad, footprint, id_biblioteca) VALUES
('Microcontrolador STM32F4',   'microcontrolador', 'STMicroelectronics', 'STM32F407VGT6',
 '{"frecuencia_mhz": 168, "ram_kb": 192, "flash_kb": 1024, "gpio": 82}',         12.50, TRUE, 'LQFP-100', 1),
('Resistencia 10kΩ 1%',        'resistencia',       'Vishay',             'CRCW060310K0FKEA',
 '{"tolerancia": "1%", "potencia_w": 0.1, "temp_coef": "100ppm"}',                0.05, TRUE, '0603',     1),
('Capacitor 100nF 50V',        'capacitor',          'Murata',             'GRM188R71H104KA93D',
 '{"capacitancia_nf": 100, "voltaje_v": 50, "tipo": "MLCC", "dielec": "X7R"}',    0.08, TRUE, '0603',     1),
('Regulador LDO 3.3V',         'regulador',          'Texas Instruments',  'LM1117MPX-3.3',
 '{"voltaje_salida_v": 3.3, "corriente_max_a": 0.8, "dropout_mv": 1200}',          1.20, TRUE, 'SOT-223',  1),
('Op-Amp de precisión',        'amplificador',       'Analog Devices',     'AD8628ARZ',
 '{"vcc_max_v": 5, "gbw_mhz": 2.5, "ib_na": 0.1, "vos_uv": 2}',                  3.80, TRUE, 'SOIC-8',   3),
('FPGA Artix-7',               'fpga',               'Xilinx',             'XC7A35T-1CPG236C',
 '{"luts": 33280, "flipflops": 41600, "bram_kb": 1800, "dsp": 90}',               25.00, TRUE, 'CPG236',   1),
('Cristal 8MHz',               'oscilador',          'Abracon',            'ABM8-8.000MHZ-B2-T',
 '{"frecuencia_mhz": 8, "tolerancia_ppm": 18, "carga_pf": 18}',                    0.45, TRUE, 'HC-49S',   4),
('Transistor NPN 2N2222',      'transistor',         'ON Semiconductor',   '2N2222A',
 '{"vceo_v": 40, "ic_max_ma": 600, "hfe_min": 100, "ft_mhz": 300}',                0.15, TRUE, 'TO-92',    1);

-- Proveedores
INSERT INTO proveedor (nombre, contacto, calificacion, tiempo_entrega, terminos_comerciales) VALUES
('Mouser Electronics',    'ventas.co@mouser.com',     4.80, 5,  'Envío gratis > USD 50. Pago 30 días.'),
('Digi-Key Colombia',     'co.sales@digikey.com',     4.90, 4,  'Envío express disponible. Pago inmediato.'),
('Arrow Electronics',     'arrow.co@arrow.com',       4.50, 7,  'Cotización por volumen. Net 30.'),
('Electronilab SAS',      'ventas@electronilab.co',   4.20, 3,  'Stock local Bogotá. Pago contra entrega.');

-- Relaciones componente-proveedor
INSERT INTO componente_proveedor (id_componente, id_proveedor, precio_proveedor, tiempo_entrega) VALUES
(1, 1, 12.50, 5), (1, 2, 11.80, 4), (1, 3, 12.00, 7),
(2, 1, 0.05,  5), (2, 2, 0.04,  4), (2, 4, 0.06,  2),
(3, 1, 0.08,  5), (3, 2, 0.07,  4),
(4, 1, 1.20,  5), (4, 2, 1.15,  4), (4, 4, 1.30,  2),
(5, 1, 3.80,  5), (5, 2, 3.65,  4),
(6, 2, 25.00, 4), (6, 3, 24.50, 7);

-- Estándares industriales
INSERT INTO estandar (nombre, tipo, version, requisitos, industria) VALUES
('IPC-A-610',   'Calidad de Ensamble',    'Rev. H', 'Criterios de aceptabilidad para ensambles electrónicos',      'Electrónica'),
('IPC-2221',    'Diseño PCB',             '2019',   'Estándar genérico de diseño de tarjetas de circuito impreso', 'Electrónica'),
('IEC 61000',   'Compatibilidad EMC',     'Ed. 4',  'Compatibilidad electromagnética - límites y métodos',         'Telecomunicaciones'),
('ISO 9001',    'Gestión de Calidad',     '2015',   'Sistema de gestión de calidad - requisitos',                  'Manufactura'),
('MIL-STD-461', 'EMI/EMC Militar',       'Rev. G', 'Requisitos para el control de interferencias electromagnéticas', 'Defensa');

-- Sistemas externos
INSERT INTO sistema_externo (nombre, tipo, url_api, configuracion, estado_conexion) VALUES
('Altium 365',    'PLM',        'https://api.altium365.com/v1',    '{"auth": "oauth2", "timeout_s": 30}', 'activo'),
('JIRA Software', 'Gestión',   'https://company.atlassian.net/rest','{"auth": "api_key", "project": "CIRC"}', 'activo'),
('SAP ERP',       'ERP',        'https://sap.empresa.co/api',       '{"auth": "basic", "client": "100"}',  'inactivo'),
('GitLab CE',     'Control Versiones', 'https://git.empresa.co/api/v4', '{"auth": "token", "group": "electronics"}', 'activo');

-- Proyectos
INSERT INTO proyecto (nombre, descripcion, tipo_circuito, estado, version_actual, id_usuario_creador) VALUES
('Tarjeta de Control de Motor BLDC',
 'Diseño de PCB para control vectorial de motor BLDC trifásico con STM32F4 y FPGA Artix-7',
 'mixto', 'activo', '1.3.0', 1),
('Módulo IoT LoRaWAN para Medición Industrial',
 'Dispositivo de adquisición de datos con comunicación LoRaWAN para industria 4.0',
 'microcontrolador', 'activo', '2.0.1', 2),
('ASIC de Procesamiento de Señal Analógica',
 'Diseño de circuito integrado de aplicación específica para filtrado adaptativo',
 'asic', 'finalizado', '3.1.0', 4),
('Plataforma FPGA para Procesamiento de Imagen',
 'Implementación de algoritmos de visión artificial en Xilinx Artix-7',
 'fpga', 'activo', '1.0.0', 2);

-- Relaciones proyecto-estándar
INSERT INTO proyecto_estandar VALUES
(1, 1), (1, 2), (1, 3), -- Proyecto 1: IPC-A-610, IPC-2221, IEC 61000
(2, 1), (2, 2), (2, 4), -- Proyecto 2: IPC-A-610, IPC-2221, ISO 9001
(3, 4), (3, 5),          -- Proyecto 3: ISO 9001, MIL-STD-461
(4, 2), (4, 3);          -- Proyecto 4: IPC-2221, IEC 61000

-- Relaciones proyecto-sistema externo
INSERT INTO proyecto_sistema_externo VALUES
(1, 1), (1, 2), (1, 4),
(2, 2), (2, 4),
(4, 1), (4, 4);

-- Diseños
INSERT INTO diseno (nombre_diseno, tipo, formato, estado, cifrado, id_proyecto, id_usuario_creador) VALUES
('Esquemático Principal Motor BLDC', 'esquematico', 'KiCad 7.0', 'aprobado',  FALSE, 1, 1),
('PCB Capa 4 Motor BLDC',            'pcb',          'KiCad 7.0', 'revision',  TRUE,  1, 2),
('Esquemático Módulo LoRaWAN',       'esquematico', 'Altium 23', 'aprobado',  FALSE, 2, 2),
('PCB Módulo LoRaWAN',               'pcb',          'Altium 23', 'aprobado',  TRUE,  2, 2),
('Layout ASIC Filtro Analógico',     'layout',       'Cadence',   'aprobado',  TRUE,  3, 4),
('Esquemático FPGA Visión',          'esquematico', 'Vivado',    'borrador',  FALSE, 4, 2);

-- Versiones de diseños
INSERT INTO version (numero_version, descripcion_cambios, autor, tag, es_release, id_diseno) VALUES
('1.0.0', 'Diseño inicial con topología básica',                   'Michael Olivares', 'v1.0',  FALSE, 1),
('1.1.0', 'Adición de protección contra sobrecorriente',           'Laura Gómez',      NULL,    FALSE, 1),
('1.2.0', 'Optimización del ruteo de potencia',                    'Michael Olivares', NULL,    FALSE, 1),
('1.3.0', 'Versión release para fabricación',                      'Michael Olivares', 'v1.3',  TRUE,  1),
('1.0.0', 'Diseño inicial PCB 4 capas',                            'Laura Gómez',      'v1.0',  FALSE, 2),
('2.0.0', 'Diseño esquemático LoRaWAN inicial',                    'Daniela Torres',   'v2.0',  TRUE,  3),
('2.0.1', 'Corrección de footprint antena',                        'Daniela Torres',   NULL,    FALSE, 3),
('2.0.0', 'PCB LoRaWAN con antena integrada',                      'Laura Gómez',      'v2.0',  TRUE,  4),
('3.1.0', 'Layout ASIC final post-síntesis',                       'Daniela Torres',   'v3.1',  TRUE,  5),
('1.0.0', 'Esquemático inicial FPGA Artix-7',                      'Laura Gómez',      NULL,    FALSE, 6);

-- Componentes en diseños
INSERT INTO diseno_componente (id_diseno, id_componente, cantidad, posicion_x, posicion_y, rotation, referencia) VALUES
(1, 1, 1,    50.000, 75.000, 0.0,   'U1'),    -- STM32F4 en esquemático motor
(1, 2, 24,   25.500, 30.000, 0.0,   'R1-R24'),-- Resistencias
(1, 3, 15,   60.000, 40.000, 90.0,  'C1-C15'),-- Capacitores
(1, 4, 2,    80.000, 20.000, 0.0,   'U2-U3'), -- Reguladores
(2, 6, 1,    100.000, 50.000, 0.0,  'U1'),    -- FPGA en PCB motor
(3, 1, 1,    45.000, 60.000, 0.0,   'U1'),    -- STM32 en LoRaWAN
(3, 7, 2,    20.000, 15.000, 0.0,   'X1-X2'), -- Cristales
(4, 2, 30,   35.000, 45.000, 0.0,   'R1-R30'),-- Resistencias
(4, 3, 20,   55.000, 35.000, 0.0,   'C1-C20'),-- Capacitores
(6, 6, 1,    90.000, 80.000, 0.0,   'U1'),    -- FPGA en visión
(6, 5, 4,    30.000, 50.000, 0.0,   'U2-U5'); -- Op-Amps

-- Simulaciones
INSERT INTO simulacion (tipo, estado, duracion, id_diseno, id_usuario) VALUES
('analogica',  'completada', 145,  1, 1),
('digital',    'completada', 320,  1, 2),
('mixta',      'completada', 860,  2, 1),
('analogica',  'completada', 95,   3, 2),
('termica',    'completada', 420,  2, 4),
('energetica', 'pendiente',  NULL, 4, 2);

-- Resultados de simulaciones
INSERT INTO resultado_simulacion (metricas, alertas, cumple_especificaciones, id_simulacion) VALUES
('{"thd": 1.2, "eficiencia": 94.5, "rizado_mv": 15}',
 'Rizado dentro del rango permitido', TRUE,  1),
('{"frecuencia_max_mhz": 168, "propagacion_ns": 5.2}',
 NULL, TRUE, 2),
('{"temperatura_max_c": 72, "punto_calor": "U1"}',
 'Temperatura próxima al límite de 85°C en U1', TRUE, 3),
('{"ganancia_db": 42.3, "ancho_banda_khz": 500, "ruido_uv": 1.8}',
 NULL, TRUE, 4),
('{"consumo_activo_mw": 245, "consumo_reposo_mw": 12}',
 NULL, TRUE, 5);

-- Pruebas
INSERT INTO prueba (tipo, resultado, id_diseno) VALUES
('funcional',    'exitosa',  1),
('consumo',      'exitosa',  1),
('emc',          'exitosa',  1),
('funcional',    'exitosa',  3),
('temperatura',  'exitosa',  3),
('stress',       'pendiente',4),
('rendimiento',  'exitosa',  5);

-- Informes de prueba
INSERT INTO informe_prueba (formato, recomendaciones, id_prueba) VALUES
('pdf', 'Diseño aprobado para fabricación. Sin observaciones.', 1),
('pdf', 'Consumo dentro de especificaciones. Eficiencia: 94.5%', 2),
('pdf', 'Cumple IEC 61000. Emisiones conducidas dentro de límites Clase B.', 3),
('pdf', 'Módulo LoRaWAN funciona correctamente en banda 915MHz.', 4),
('pdf', 'Opera correctamente entre -20°C y +70°C. Rango extendido verificado.', 5),
('pdf', 'ASIC cumple especificaciones de rendimiento. SNR: 68dB.', 7);

-- Firmware
INSERT INTO firmware (nombre, version, arquitectura, fecha_compilacion, id_diseno) VALUES
('Motor BLDC Control FW',   '1.3.0', 'ARM Cortex-M4',  '2026-01-15 14:30:00', 1),
('LoRaWAN Node FW',          '2.0.1', 'ARM Cortex-M0+', '2026-01-28 09:00:00', 3);

-- Documentación generada
INSERT INTO documentacion (tipo, formato, nivel_detalle, plantilla_utilizada, id_proyecto) VALUES
('Manual de Diseño',          'pdf',  'completo',   'SENA-ADSO-TechDoc-v2', 1),
('Memoria de Cálculo',        'pdf',  'detallado',  'SENA-ADSO-Calc-v1',    1),
('Informe de Validación',     'pdf',  'resumen',    'SENA-ADSO-Valid-v1',   2),
('Especificaciones Técnicas', 'docx', 'completo',   'SENA-ADSO-TechSpec-v1',3),
('Reporte de Cierre',         'pdf',  'ejecutivo',  'SENA-ADSO-Close-v1',   3);

-- ============================================================
-- SECCIÓN 7: CONSULTAS DE VERIFICACIÓN
-- ============================================================

-- Verificar tablas creadas
SELECT table_name, table_rows, engine
FROM information_schema.tables
WHERE table_schema = 'sistema_diseno_circuitos'
ORDER BY table_name;

-- Contar registros por tabla principal
SELECT 'usuario'    AS tabla, COUNT(*) AS registros FROM usuario    UNION ALL
SELECT 'proyecto',            COUNT(*)               FROM proyecto   UNION ALL
SELECT 'diseno',              COUNT(*)               FROM diseno     UNION ALL
SELECT 'version',             COUNT(*)               FROM version    UNION ALL
SELECT 'componente',          COUNT(*)               FROM componente UNION ALL
SELECT 'proveedor',           COUNT(*)               FROM proveedor  UNION ALL
SELECT 'simulacion',          COUNT(*)               FROM simulacion UNION ALL
SELECT 'prueba',              COUNT(*)               FROM prueba     UNION ALL
SELECT 'firmware',            COUNT(*)               FROM firmware;

-- Proyectos activos con su creador (usando la vista)
SELECT * FROM v_proyectos_activos;

-- Resumen de diseños por proyecto (usando la vista)
SELECT * FROM v_resumen_disenos;

-- ============================================================
-- FIN DEL SCRIPT - GA6-220501096-AA2-EV03
-- TOTAL TABLAS: 21 | VISTAS: 5 | PROCEDIMIENTOS: 4 | TRIGGERS: 3
-- Normalización: 3FN / BCNF 
-- Motor: MySQL 8.x (InnoDB)
-- Herramienta: MySQL Workbench
-- ============================================================