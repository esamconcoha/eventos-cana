-- =========================================================
-- Migracion 008: trazabilidad de estado por TRIGGER en pedidos y cotizaciones.
--
-- Modelo nuevo (igual para las dos entidades):
--   * La tabla maestra    -> pedidos_cana.estado_pedido / cotizaciones.estado_cotizacion
--     guarda SIEMPRE el CODIGO del estado ACTUAL (denormalizado, lectura barata).
--   * La tabla muchos-a-muchos -> estados_pedido / estados_cotizacion
--     guarda el HISTORIAL completo (un tramo por estado, con inicio y fin).
--   * Un trigger AFTER en la tabla maestra mantiene el historial solo: cada vez
--     que cambia el codigo, cierra el tramo abierto y abre el nuevo. El backend
--     ya NO escribe el historial a mano (ver notas del backend mas abajo).
--
-- ORDEN OBLIGATORIO dentro de este script:
--   1) backfill de los datos          <- se corre ANTES de crear el trigger,
--   2) seed del historial de cotizaciones  para que el propio backfill NO
--   3) creacion de funciones y triggers     dispare el trigger y duplique tramos.
--
-- Nota de tipo: se asume que pedidos_cana.estado_pedido ya es varchar(10)
-- (el cambio de boolean->varchar dejo todas las filas en 'true'; el backfill
--  del paso 1 las reemplaza por el codigo real). Si se corre en un ambiente
-- donde todavia es boolean, primero:
--     ALTER TABLE cana.pedidos_cana
--         ALTER COLUMN estado_pedido TYPE varchar(10) USING NULL;   -- se rellena abajo
-- =========================================================

BEGIN;

-- ---------------------------------------------------------
-- 1) BACKFILL de pedidos_cana.estado_pedido
--    El estado real nunca se perdio: es el codigo del tramo ABIERTO en
--    estados_pedido (fecha_hora_fin IS NULL), que la app venia manteniendo.
--    La columna booleana solo servia de bandera "activo" y siempre valia true.
-- ---------------------------------------------------------
UPDATE cana.pedidos_cana p
   SET estado_pedido = e.codigo_estado
  FROM cana.estados_pedido ep
  JOIN cana.estados e ON e.id_estado = ep.id_estado
 WHERE ep.correlativo_pedido = p.correlativo_pedido
   AND ep.fecha_hora_fin IS NULL;

-- Red de seguridad: cualquier pedido que quedo con un valor invalido
-- (p.ej. el 'true' del cast, o sin tramo abierto) se lleva al estado inicial.
UPDATE cana.pedidos_cana
   SET estado_pedido = 'CNF'   -- ESTADO_EVENTO_INICIAL
 WHERE estado_pedido IS NULL
    OR estado_pedido NOT IN (
        SELECT codigo_estado FROM cana.estados WHERE tipo_estado = 'EVE');

-- ---------------------------------------------------------
-- 2) SEED del historial de cotizaciones
--    A diferencia de los pedidos, el historial de cotizaciones nunca se
--    escribio (la app solo mantenia la columna estado_cotizacion). Se siembra
--    un tramo abierto por cada cotizacion existente con su estado actual, para
--    que a partir de aqui el trigger tenga un tramo que cerrar en el 1er cambio.
--    Es un INSERT directo a la tabla historica: NO dispara el trigger de
--    cotizaciones (ese trigger esta sobre cotizaciones, no sobre esta tabla).
-- ---------------------------------------------------------
INSERT INTO cana.estados_cotizacion (id_cotizacion, id_estado, fecha_hora_inicio, fecha_hora_fin)
SELECT c.id_cotizacion,
       e.id_estado,
       COALESCE(c.fecha_cotizacion::timestamp, now()),
       NULL
  FROM cana.cotizaciones c
  JOIN cana.estados e
    ON e.tipo_estado = 'COT'
   AND e.codigo_estado = c.estado_cotizacion
 WHERE NOT EXISTS (
        SELECT 1 FROM cana.estados_cotizacion ec
         WHERE ec.id_cotizacion = c.id_cotizacion);

-- =========================================================
-- 3) FUNCION + TRIGGER de trazabilidad de PEDIDOS
-- =========================================================
CREATE OR REPLACE FUNCTION cana.fn_trazabilidad_estado_pedido()
RETURNS TRIGGER AS $$
DECLARE
    v_id_estado int8;
BEGIN
    -- En UPDATE, si el codigo no cambio no hay nada que historiar.
    IF (TG_OP = 'UPDATE' AND NEW.estado_pedido IS NOT DISTINCT FROM OLD.estado_pedido) THEN
        RETURN NEW;
    END IF;

    -- Defensivo: sin codigo no se historia (la columna deberia venir siempre).
    IF (NEW.estado_pedido IS NULL) THEN
        RETURN NEW;
    END IF;

    -- Resolver el id numerico del estado desde el codigo actual.
    -- Se filtra por tipo_estado para no colisionar con los codigos de cotizacion.
    SELECT id_estado INTO v_id_estado
      FROM cana.estados
     WHERE tipo_estado = 'EVE'
       AND codigo_estado = NEW.estado_pedido;

    -- Fallar rapido si el codigo no existe: id_estado es FK, insertar NULL
    -- reventaria con un error mucho menos claro que este.
    IF (v_id_estado IS NULL) THEN
        RAISE EXCEPTION 'Estado de pedido "%" no existe en cana.estados (tipo_estado=EVE)', NEW.estado_pedido;
    END IF;

    -- Cerrar el tramo abierto (solo aplica cuando ya habia uno, es decir UPDATE).
    IF (TG_OP = 'UPDATE') THEN
        UPDATE cana.estados_pedido
           SET fecha_hora_fin = now()
         WHERE correlativo_pedido = NEW.correlativo_pedido
           AND fecha_hora_fin IS NULL;
    END IF;

    -- Abrir el tramo nuevo (queda abierto hasta el proximo cambio).
    INSERT INTO cana.estados_pedido (
        correlativo_pedido, id_estado, fecha_hora_inicio, fecha_hora_fin)
    VALUES (
        NEW.correlativo_pedido, v_id_estado, now(), NULL);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_trazabilidad_estado_pedido ON cana.pedidos_cana;
CREATE TRIGGER trg_trazabilidad_estado_pedido
AFTER INSERT OR UPDATE OF estado_pedido ON cana.pedidos_cana
FOR EACH ROW
EXECUTE PROCEDURE cana.fn_trazabilidad_estado_pedido();

-- =========================================================
-- 4) FUNCION + TRIGGER de trazabilidad de COTIZACIONES
-- =========================================================
CREATE OR REPLACE FUNCTION cana.fn_trazabilidad_estado_cotizacion()
RETURNS TRIGGER AS $$
DECLARE
    v_id_estado int8;
BEGIN
    IF (TG_OP = 'UPDATE' AND NEW.estado_cotizacion IS NOT DISTINCT FROM OLD.estado_cotizacion) THEN
        RETURN NEW;
    END IF;

    IF (NEW.estado_cotizacion IS NULL) THEN
        RETURN NEW;
    END IF;

    SELECT id_estado INTO v_id_estado
      FROM cana.estados
     WHERE tipo_estado = 'COT'
       AND codigo_estado = NEW.estado_cotizacion;

    IF (v_id_estado IS NULL) THEN
        RAISE EXCEPTION 'Estado de cotizacion "%" no existe en cana.estados (tipo_estado=COT)', NEW.estado_cotizacion;
    END IF;

    IF (TG_OP = 'UPDATE') THEN
        UPDATE cana.estados_cotizacion
           SET fecha_hora_fin = now()
         WHERE id_cotizacion = NEW.id_cotizacion
           AND fecha_hora_fin IS NULL;
    END IF;

    INSERT INTO cana.estados_cotizacion (
        id_cotizacion, id_estado, fecha_hora_inicio, fecha_hora_fin)
    VALUES (
        NEW.id_cotizacion, v_id_estado, now(), NULL);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_trazabilidad_estado_cotizacion ON cana.cotizaciones;
CREATE TRIGGER trg_trazabilidad_estado_cotizacion
AFTER INSERT OR UPDATE OF estado_cotizacion ON cana.cotizaciones
FOR EACH ROW
EXECUTE PROCEDURE cana.fn_trazabilidad_estado_cotizacion();

COMMIT;

-- =========================================================
-- VERIFICACION POSTERIOR (correr a mano, no forma parte de la migracion)
--
--   -- a) Estado actual real de cada pedido (columna vs tramo abierto: deben coincidir)
--   SELECT p.correlativo_pedido,
--          p.estado_pedido                       AS codigo_en_columna,
--          e.codigo_estado                       AS codigo_en_tramo_abierto,
--          e.nombre_estado
--     FROM cana.pedidos_cana p
--     LEFT JOIN cana.estados_pedido ep
--            ON ep.correlativo_pedido = p.correlativo_pedido
--           AND ep.fecha_hora_fin IS NULL
--     LEFT JOIN cana.estados e ON e.id_estado = ep.id_estado
--    ORDER BY p.correlativo_pedido;
--
--   -- b) Integridad: ningun pedido/cotizacion debe tener mas de un tramo abierto
--   SELECT correlativo_pedido, count(*)
--     FROM cana.estados_pedido WHERE fecha_hora_fin IS NULL
--    GROUP BY correlativo_pedido HAVING count(*) > 1;
--
--   SELECT id_cotizacion, count(*)
--     FROM cana.estados_cotizacion WHERE fecha_hora_fin IS NULL
--    GROUP BY id_cotizacion HAVING count(*) > 1;
--
--   -- c) Prueba en vivo (en una transaccion que luego se hace ROLLBACK):
--   --    BEGIN;
--   --      UPDATE cana.pedidos_cana SET estado_pedido = 'EP'
--   --       WHERE correlativo_pedido = '<algun_pedido>';
--   --      SELECT * FROM cana.estados_pedido
--   --       WHERE correlativo_pedido = '<algun_pedido>' ORDER BY fecha_hora_inicio;
--   --    ROLLBACK;
-- =========================================================
