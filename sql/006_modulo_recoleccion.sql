-- =========================================================
-- Migracion: soporte para el modulo de recoleccion.
--
-- La recoleccion tiene exactamente la misma forma que la entrega: N viajes,
-- cada uno con items y cantidades. Por eso detalle_viaje y detalle_viaje_items
-- se reusan SIN NINGUN CAMBIO, y lo unico que se agrega es un discriminador
-- en la tabla padre: cada pedido pasa a tener hasta dos filas en
-- entregas_pedido, una de ida ('ENT') y una de vuelta ('REC').
--
-- Cada movimiento conserva sus propios contadores de viajes, sus fechas de
-- ejecucion y su bandera de finalizado, que es justo lo que hace falta.
--
-- El nombre de la tabla queda impreciso (pasa a ser "movimiento logistico"),
-- pero renombrarla arrastraria la entidad, 5 consultas nativas y el idEntrega
-- de todo el contrato del frontend. No lo vale.
-- =========================================================

BEGIN;

-- ---------------------------------------------------------
-- 0) Verificacion previa. Se espera que NO devuelva filas: si alguna aparece,
--    hay dos entregas para un mismo pedido y el indice unico nuevo fallaria.
--
--    SELECT correlativo_pedido, COUNT(*) FROM cana.entregas_pedido
--     GROUP BY correlativo_pedido HAVING COUNT(*) > 1;
-- ---------------------------------------------------------

-- 1) Discriminador. El DEFAULT deja todas las filas existentes como 'ENT',
--    que es lo que efectivamente son: no hace falta backfill aparte.
ALTER TABLE cana.entregas_pedido
    ADD COLUMN IF NOT EXISTS tipo_movimiento VARCHAR(3) NOT NULL DEFAULT 'ENT';

ALTER TABLE cana.entregas_pedido
    ADD CONSTRAINT entregas_pedido_tipo_movimiento_chk
        CHECK (tipo_movimiento IN ('ENT', 'REC'));

COMMENT ON COLUMN cana.entregas_pedido.tipo_movimiento IS
    'ENT = entrega (ida), REC = recoleccion (vuelta). Un pedido tiene a lo sumo una fila de cada tipo.';

-- 2) La unicidad pasa a ser por pedido Y tipo: un pedido puede tener su
--    entrega y su recoleccion, pero no dos entregas ni dos recolecciones.
--    Se crea el indice nuevo antes de soltar el viejo; en este punto todas
--    las filas son 'ENT', asi que ninguno de los dos puede violarse.
CREATE UNIQUE INDEX IF NOT EXISTS idx_entregas_pedido_correlativo_tipo
    ON cana.entregas_pedido (correlativo_pedido, tipo_movimiento);

DROP INDEX IF EXISTS cana.idx_entregas_pedido_correlativo;

-- 3) La recoleccion se agenda con pedidos_cana.fecha_recogido, que ya existe
--    (DATE, igual que fecha_entrega) y hasta hoy no la escribia ningun flujo.
--    Queda la misma simetria que ya tiene la entrega:
--
--      planificado            ejecutado
--      ------------------     -------------------------------------------
--      fecha_entrega     ->   fila 'ENT': fecha_inicio_entrega / fecha_fin_entrega
--      fecha_recogido    ->   fila 'REC': fecha_inicio_entrega / fecha_fin_entrega
--
--    El nombre suena a pasado pero se usa como fecha PROGRAMADA. Se documenta
--    en la BD para que nadie lo interprete al reves.
COMMENT ON COLUMN cana.pedidos_cana.fecha_recogido IS
    'Fecha PROGRAMADA de recoleccion (agenda). Lo realmente ejecutado vive en la fila REC de entregas_pedido.';

COMMENT ON COLUMN cana.pedidos_cana.fecha_entrega IS
    'Fecha PACTADA de entrega (agenda). Lo realmente ejecutado vive en la fila ENT de entregas_pedido.';

-- 4) No se agrega indice sobre tipo_movimiento a proposito: con solo dos
--    valores posibles el filtro no es selectivo y Postgres igual haria scan.
--    El indice unico del punto 2 ya cubre las busquedas por correlativo.

COMMIT;

-- =========================================================
-- Verificacion posterior
--
--   -- Todas las filas viejas quedaron como entrega:
--   SELECT tipo_movimiento, COUNT(*) FROM cana.entregas_pedido GROUP BY 1;
--
--   -- El CHECK rechaza cualquier otro valor (debe fallar):
--   -- INSERT INTO cana.entregas_pedido (correlativo_pedido, tipo_movimiento)
--   --   VALUES ('PED-2026-00001', 'XXX');
--
--   -- Los indices quedaron intercambiados:
--   SELECT indexname FROM pg_indexes
--    WHERE schemaname = 'cana' AND tablename = 'entregas_pedido';
-- =========================================================
