-- =========================================================
-- Migracion: el montaje deja de ser un estado del pedido, y se retiran de
-- circulacion los dos estados que no representaban un hecho observable.
--
-- 1) "Montando / Decorando" (M) no puede ser un estado porque es PARALELO:
--    mientras el equipo decora pueden seguir saliendo viajes de entrega, y una
--    maquina de estados es excluyente (el pedido esta en uno solo a la vez).
--    Ademas es opcional: solo aplica a los pedidos que compraron el servicio.
--    Pasa a ser un dato de la linea de servicio, que es donde corresponde.
--
-- 2) "Pendiente de recoleccion" (PR) nunca lo escribio nadie, y el dato que
--    representaba ya se deriva de entregas_pedido: una recoleccion sin
--    fecha_recogido ES una recoleccion pendiente de agendar. Mantenerlo a mano
--    seria una segunda fuente de verdad del mismo hecho.
-- =========================================================

BEGIN;

-- ---------------------------------------------------------
-- 1) Cuando se realizo el servicio. NULL = todavia no.
--    Timestamp y no booleano: responde lo mismo (IS NOT NULL) y ademas deja
--    registrado el cuando, que sirve para agenda y auditoria y no cuesta nada.
-- ---------------------------------------------------------
ALTER TABLE cana.detalle_servicio_pedido
    ADD COLUMN IF NOT EXISTS fecha_realizado TIMESTAMP NULL;

COMMENT ON COLUMN cana.detalle_servicio_pedido.fecha_realizado IS
    'Momento en que se confirmo realizado el servicio (montaje, decoracion...). NULL = pendiente.';

-- ---------------------------------------------------------
-- 2) Se DESACTIVAN, no se borran: si algun estados_pedido historico los
--    referencia, borrar la fila rompe el historial y los joins que lo leen.
--    Con estado_registro = false salen de circulacion sin perder el pasado.
-- ---------------------------------------------------------
UPDATE cana.estados
   SET estado_registro = false
 WHERE tipo_estado = 'EVE'
   AND codigo_estado IN ('M', 'PR');

COMMIT;

-- =========================================================
-- Verificacion posterior
--
--   -- Deben quedar activos: CNF, EP, RE, ETR, REC, FIN
--   SELECT codigo_estado, nombre_estado, estado_registro
--     FROM cana.estados WHERE tipo_estado = 'EVE' ORDER BY id_estado;
--
--   -- Pedidos que quedaron parados en un estado ya desactivado: hay que
--   -- moverlos a mano al estado que corresponda.
--   SELECT ep.correlativo_pedido, e.codigo_estado, e.nombre_estado
--     FROM cana.estados_pedido ep
--     JOIN cana.estados e ON e.id_estado = ep.id_estado
--    WHERE ep.fecha_hora_fin IS NULL
--      AND e.codigo_estado IN ('M', 'PR');
--
--   -- Servicios pendientes de confirmar
--   SELECT correlativo_pedido, id_servicio, fecha_realizado
--     FROM cana.detalle_servicio_pedido WHERE fecha_realizado IS NULL;
-- =========================================================
