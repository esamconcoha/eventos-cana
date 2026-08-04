-- =========================================================
-- Migracion: manejo de pagos (anticipos, abonos, historial de estado)
-- Reemplaza el booleano pagado en pedidos_cana por un manejo real
-- de pagos parciales/anticipos, con historial de estado de pago.
-- =========================================================

BEGIN;

-- =========================================================
-- 1) Catalogo: estados de pago (tipo_estado = 'PAGO')
-- =========================================================
INSERT INTO cana.estados (tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro)
VALUES
    ('PAGO', 'PENDIENTE', 'Pendiente de pago', 'El pedido aun no tiene ningun pago registrado',       true),
    ('PAGO', 'ANTICIPO',  'Anticipo recibido', 'Se recibio un anticipo pero no cubre el total',       true),
    ('PAGO', 'PARCIAL',   'Pago parcial',      'Se han recibido abonos pero no se completa el total', true),
    ('PAGO', 'PAGADO',    'Pagado completo',   'El pedido esta completamente pagado',                 true),
    ('PAGO', 'DEVUELTO',  'Devuelto',          'Se devolvio el pago al cliente',                      true);

-- =========================================================
-- 2) Catalogo: tipos de pago (tipo_estado = 'TIPO_PAGO')
-- =========================================================
INSERT INTO cana.estados (tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro)
VALUES
    ('TIPO_PAGO', 'ANTICIPO',   'Anticipo',   'Pago inicial antes de completar el pedido', true),
    ('TIPO_PAGO', 'ABONO',      'Abono',      'Pago parcial adicional al anticipo',        true),
    ('TIPO_PAGO', 'PAGO_FINAL', 'Pago final', 'Pago que completa el saldo del pedido',      true),
    ('TIPO_PAGO', 'DEVOLUCION', 'Devolucion', 'Devolucion de dinero al cliente',            true);

-- =========================================================
-- 3) Tabla de pagos (anticipos, abonos, pago final, devoluciones)
--    Validaciones de tipo_pago/metodo_pago/monto se manejan en backend
-- =========================================================
CREATE TABLE cana.pagos_pedido (
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

CREATE INDEX idx_pagos_pedido_correlativo_pedido ON cana.pagos_pedido (correlativo_pedido);

-- =========================================================
-- 4) pedidos_cana: estado_pago como codigo (igual que estado_cotizacion)
-- =========================================================
ALTER TABLE cana.pedidos_cana
    ADD COLUMN estado_pago VARCHAR(10);

UPDATE cana.pedidos_cana
SET estado_pago = CASE WHEN pagado = TRUE THEN 'PAGADO' ELSE 'PENDIENTE' END;

ALTER TABLE cana.pedidos_cana
    ALTER COLUMN estado_pago SET NOT NULL;

ALTER TABLE cana.pedidos_cana
    DROP COLUMN pagado;

CREATE INDEX idx_pedidos_cana_estado_pago ON cana.pedidos_cana (estado_pago);

-- =========================================================
-- 5) OPCIONAL pero recomendado: precio congelado en detalle_pedido
--    Sin esto, el saldo pendiente de pedidos viejos cambia si
--    costo_item se actualiza despues de creado el pedido
--    (detalle_servicio_pedido ya tiene precio_acordado; detalle_pedido no).
-- =========================================================
ALTER TABLE cana.detalle_pedido
    ADD COLUMN costo_acordado DOUBLE PRECISION;

-- =========================================================
-- 6) Historial de estados de pago (mismo patron que estados_pedido)
--    El trigger que se agregue despues debe:
--      a) cerrar el registro vigente: UPDATE ... SET fecha_hora_fin = now()
--         WHERE correlativo_pedido = X AND fecha_hora_fin IS NULL
--      b) insertar el nuevo con fecha_hora_inicio = now() y fecha_hora_fin = NULL
-- =========================================================
CREATE TABLE cana.estados_pago_pedido (
    correlativo_estado_pago BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correlativo_pedido      VARCHAR(50) NOT NULL,
    estado_pago             VARCHAR(10) NOT NULL,
    fecha_hora_inicio       TIMESTAMP   NOT NULL DEFAULT now(),
    fecha_hora_fin          TIMESTAMP,

    CONSTRAINT fk_estados_pago_pedido_pedido
        FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana (correlativo_pedido)
);

CREATE INDEX idx_estados_pago_pedido_correlativo_pedido ON cana.estados_pago_pedido (correlativo_pedido);

-- registro inicial: deja abierto el estado que ya quedo en pedidos_cana.estado_pago
INSERT INTO cana.estados_pago_pedido (correlativo_pedido, estado_pago, fecha_hora_inicio, fecha_hora_fin)
SELECT correlativo_pedido, estado_pago, now(), NULL
FROM cana.pedidos_cana;

COMMIT;
