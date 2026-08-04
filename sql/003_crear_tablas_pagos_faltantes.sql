-- =========================================================
-- Fix: cana.pagos_pedido y cana.estados_pago_pedido nunca se crearon
-- (error "no existe la relacion cana.estados_pago_pedido" al registrar un pago).
-- No toca pedidos_cana ni catalogos existentes.
-- =========================================================

BEGIN;

CREATE TABLE IF NOT EXISTS cana.pagos_pedido (
    id_pago            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correlativo_pedido VARCHAR(50)      NOT NULL,
    monto_pago         DOUBLE PRECISION NOT NULL,
    fecha_pago         TIMESTAMP        NOT NULL DEFAULT now(),
    tipo_pago          VARCHAR(10)      NOT NULL,
    metodo_pago        VARCHAR(20)      NOT NULL,
    referencia_pago    VARCHAR(100),
    usuario_registro   VARCHAR(50)      NOT NULL,
    estado_registro    BOOLEAN          NOT NULL DEFAULT TRUE,
    fecha_creo         TIMESTAMP        NOT NULL DEFAULT now(),

    CONSTRAINT fk_pagos_pedido_pedido
        FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana (correlativo_pedido),
    CONSTRAINT fk_pagos_pedido_usuario
        FOREIGN KEY (usuario_registro) REFERENCES cana.usuarios (dpi_nit_usuario)
);

CREATE INDEX IF NOT EXISTS idx_pagos_pedido_correlativo_pedido ON cana.pagos_pedido (correlativo_pedido);

CREATE TABLE IF NOT EXISTS cana.estados_pago_pedido (
    correlativo_estado_pago BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correlativo_pedido      VARCHAR(50) NOT NULL,
    estado_pago             VARCHAR(10) NOT NULL,
    fecha_hora_inicio       TIMESTAMP   NOT NULL DEFAULT now(),
    fecha_hora_fin          TIMESTAMP,

    CONSTRAINT fk_estados_pago_pedido_pedido
        FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana (correlativo_pedido)
);

CREATE INDEX IF NOT EXISTS idx_estados_pago_pedido_correlativo_pedido ON cana.estados_pago_pedido (correlativo_pedido);

-- registro inicial: deja abierto el estado actual de cada pedido
-- (si pedidos_cana.estado_pago quedo NULL porque no corriste el UPDATE de
-- la migracion original, se usa PENDIENTE como valor por defecto).
INSERT INTO cana.estados_pago_pedido (correlativo_pedido, estado_pago, fecha_hora_inicio, fecha_hora_fin)
SELECT correlativo_pedido, COALESCE(estado_pago, 'PENDIENTE'), now(), NULL
FROM cana.pedidos_cana
WHERE NOT EXISTS (
    SELECT 1 FROM cana.estados_pago_pedido epp WHERE epp.correlativo_pedido = pedidos_cana.correlativo_pedido
);

COMMIT;
