-- =========================================================
-- Migracion: soporte para pedidos "directos" (sin cotizacion previa)
-- y enlace pedido <- cotizacion cuando esta se confirma.
--
-- nombre_cliente_pedidoprov, telefono_cliente_pedido (int4) e id_cotizacion
-- (int4) ya existen en la BD real (confirmado), no se tocan aqui.
-- =========================================================

BEGIN;

-- dpi_usuario_pedido se vuelve opcional: solo aplica cuando el pedido esta
-- ligado a una cuenta de cliente registrada (pedidos directos usan
-- nombre_cliente_pedidoprov / telefono_cliente_pedido en su lugar).
ALTER TABLE cana.pedidos_cana
    ALTER COLUMN dpi_usuario_pedido DROP NOT NULL;

-- Enlace al origen (cotizacion confirmada). Si la columna ya tiene la FK,
-- este ALTER fallara con "constraint already exists" -- en ese caso omitelo.
ALTER TABLE cana.pedidos_cana
    ADD CONSTRAINT fk_pedidos_cana_cotizacion
        FOREIGN KEY (id_cotizacion) REFERENCES cana.cotizaciones (id_cotizacion);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pedidos_cana_id_cotizacion
    ON cana.pedidos_cana (id_cotizacion)
    WHERE id_cotizacion IS NOT NULL;

-- salon_entrega se queda como esta (Long): habra un mantenimiento de salones
-- propio y salon_entrega sera FK a esa tabla cuando exista.

-- direccion_pedido deja de ser FK a cana.direcciones y pasa a texto libre,
-- igual que cotizaciones.direccion_cliente_cotizacion (un pedido directo no
-- siempre tiene un dpi_usuario_pedido al cual atarle una direccion guardada).
ALTER TABLE cana.pedidos_cana
    ALTER COLUMN direccion_pedido TYPE VARCHAR(250) USING direccion_pedido::varchar;

COMMIT;

-- =========================================================
-- Dato a corregir manualmente (no lo hago yo para no tocar datos sin que
-- lo confirmes): la fila de "Finalizado" quedo con tipo_estado='EVENTO'
-- en vez de 'EVE', inconsistente con el resto del grupo de estados de pedido.
-- =========================================================
-- UPDATE cana.estados SET tipo_estado = 'EVE' WHERE id_estado = 13;
