-- =========================================================
-- Migracion: constancia de entrega firmada.
--
-- Mismo patron que cana.documentos_cotizacion (documento generado + guardado
-- para consultarlo despues), pero acá el contenido no lo genera el sistema:
-- lo sube el usuario despues de que el cliente firma la constancia impresa
-- (foto o escaneo). tipo_documento distingue el caso por si mas adelante se
-- quiere guardar tambien la version generada sin firmar.
-- =========================================================

BEGIN;

CREATE TABLE cana.documentos_entrega (
    -- BIGSERIAL y no "GENERATED ALWAYS AS IDENTITY": el entorno de desarrollo
    -- corre PostgreSQL 9.6 (esa sintaxis es de PG10+); documentos_cotizacion,
    -- la tabla que este script espeja, tambien esta armada asi.
    id_documento      BIGSERIAL PRIMARY KEY,
    id_entrega        BIGINT NOT NULL,
    nombre_documento  VARCHAR(150),
    tipo_documento    VARCHAR(30),
    content_type      VARCHAR(60),
    contenido         BYTEA,
    fecha_generacion  TIMESTAMP,
    usuario_genero    VARCHAR(50),
    estado_registro   BOOLEAN DEFAULT TRUE,

    CONSTRAINT documentos_entrega_id_entrega_fkey
        FOREIGN KEY (id_entrega) REFERENCES cana.entregas_pedido (id_entrega)
);

CREATE INDEX idx_documentos_entrega_id_entrega ON cana.documentos_entrega (id_entrega);

COMMIT;
