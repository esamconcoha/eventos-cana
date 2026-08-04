-- =========================================================
-- Migracion: integridad para el modulo de control de entregas
-- (entregas_pedido -> detalle_viaje -> detalle_viaje_items).
--
-- Las tres tablas ya existen; aqui se cierran los huecos que impiden
-- confiar en los datos: FKs opcionales, id_item sin FK, y la posibilidad
-- de crear dos entregas para el mismo pedido.
-- =========================================================

BEGIN;

-- ---------------------------------------------------------
-- 0) Verificacion previa. Si alguno de estos SELECT devuelve filas,
--    los ALTER ... SET NOT NULL de mas abajo van a fallar. Hay que
--    decidir que hacer con esas filas huerfanas ANTES de correr esto.
--
--    SELECT * FROM cana.detalle_viaje        WHERE id_entrega IS NULL;
--    SELECT * FROM cana.detalle_viaje_items  WHERE id_detalle_viaje IS NULL OR id_item IS NULL;
--    SELECT * FROM cana.entregas_pedido      WHERE correlativo_pedido IS NULL;
--    SELECT correlativo_pedido FROM cana.entregas_pedido
--      GROUP BY correlativo_pedido HAVING COUNT(*) > 1;
-- ---------------------------------------------------------

-- 1) Las FK del arbol de entregas son obligatorias por negocio:
--    un viaje siempre pertenece a una entrega, y un item cargado
--    siempre pertenece a un viaje.
ALTER TABLE cana.entregas_pedido     ALTER COLUMN correlativo_pedido  SET NOT NULL;
ALTER TABLE cana.detalle_viaje       ALTER COLUMN id_entrega          SET NOT NULL;
ALTER TABLE cana.detalle_viaje_items ALTER COLUMN id_detalle_viaje    SET NOT NULL;
ALTER TABLE cana.detalle_viaje_items ALTER COLUMN id_item             SET NOT NULL;

-- 2) detalle_viaje_items.id_item no tenia FK: hoy nada impide guardar
--    un item inexistente en un viaje.
ALTER TABLE cana.detalle_viaje_items
    ADD CONSTRAINT detalle_viaje_items_item_fk
        FOREIGN KEY (id_item) REFERENCES cana.items_cana (id_item);

-- 3) Un pedido tiene a lo sumo una entrega (es el supuesto sobre el que
--    se construye pedidosDisponibles: el pedido sale del listado en
--    cuanto se le crea la entrega).
CREATE UNIQUE INDEX IF NOT EXISTS idx_entregas_pedido_correlativo
    ON cana.entregas_pedido (correlativo_pedido);

-- 4) cantidad_viajes_reales es un contador que mantiene el backend en
--    registrarViaje. Default 0 para que nunca arranque en NULL.
ALTER TABLE cana.entregas_pedido
    ALTER COLUMN cantidad_viajes_reales SET DEFAULT 0;

UPDATE cana.entregas_pedido SET cantidad_viajes_reales = 0 WHERE cantidad_viajes_reales IS NULL;
UPDATE cana.entregas_pedido SET pedido_finalizado = false WHERE pedido_finalizado IS NULL;

-- 5) Indices para los accesos del modulo (siempre se navega hacia abajo:
--    entrega -> sus viajes -> los items de esos viajes).
CREATE INDEX IF NOT EXISTS idx_detalle_viaje_id_entrega
    ON cana.detalle_viaje (id_entrega);
CREATE INDEX IF NOT EXISTS idx_detalle_viaje_items_id_detalle_viaje
    ON cana.detalle_viaje_items (id_detalle_viaje);
-- viajesHoy filtra por rango de fecha_inicio_viaje.
CREATE INDEX IF NOT EXISTS idx_detalle_viaje_fecha_inicio
    ON cana.detalle_viaje (fecha_inicio_viaje);

-- 6) Correccion de dato pendiente desde 002: la fila "Finalizado" quedo
--    con tipo_estado='EVENTO' en vez de 'EVE'. Se aplica ahora porque
--    marcarFinalizada mueve el pedido dentro del grupo de estados 'EVE'
--    y un grupo inconsistente rompe los filtros por tipo_estado.
UPDATE cana.estados SET tipo_estado = 'EVE'
 WHERE tipo_estado = 'EVENTO' AND codigo_estado = 'FIN';

COMMIT;

-- =========================================================
-- Verificacion posterior: el grupo de estados del ciclo de vida del
-- pedido debe quedar completo bajo tipo_estado = 'EVE'.
-- Se esperan: CNF, EP, M, RE, ETR, PR, REC, FIN.
--
--   SELECT id_estado, codigo_estado, nombre_estado
--     FROM cana.estados WHERE tipo_estado = 'EVE' ORDER BY id_estado;
--
-- Si faltan RE (En ruta entrega) o ETR (Entregado), registrarViaje y
-- marcarFinalizada van a devolver el error 4022.
-- =========================================================
