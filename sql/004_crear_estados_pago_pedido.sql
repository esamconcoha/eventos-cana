-- =========================================================
-- Historial de estados de pago por pedido (mismo patron que estados_pedido).
-- No es catalogo (el catalogo de codigos sigue viviendo en cana.estados,
-- tipo_estado='PAGO'): esta tabla solo registra cuando cada pedido entro
-- y salio de cada estado.
-- =========================================================

BEGIN;

CREATE TABLE IF NOT EXISTS cana.estados_pago_pedido (
    correlativo_estado_pago BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correlativo_pedido      VARCHAR(50) NOT NULL,
    estado_pago             VARCHAR(10) NOT NULL,
    fecha_hora_inicio       TIMESTAMP   NOT NULL DEFAULT now(),
    fecha_hora_fin          TIMESTAMP,

    CONSTRAINT fk_estados_pago_pedido_pedido
        FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana (correlativo_pedido)
);

CREATE INDEX IF NOT EXISTS idx_estados_pago_pedido_correlativo_pedido
    ON cana.estados_pago_pedido (correlativo_pedido);

-- registro inicial: deja abierto el estado actual de cada pedido
-- (si pedidos_cana.estado_pago quedo NULL, se usa PENDIENTE por defecto)
INSERT INTO cana.estados_pago_pedido (correlativo_pedido, estado_pago, fecha_hora_inicio, fecha_hora_fin)
SELECT correlativo_pedido, COALESCE(estado_pago, 'PENDIENTE'), now(), NULL
FROM cana.pedidos_cana
WHERE NOT EXISTS (
    SELECT 1 FROM cana.estados_pago_pedido epp WHERE epp.correlativo_pedido = pedidos_cana.correlativo_pedido
);

COMMIT;
