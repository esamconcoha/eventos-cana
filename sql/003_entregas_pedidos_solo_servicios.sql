--
-- Pedidos de solo servicios: se les borra la entrega vacia.
--
-- Un pedido sin items no tiene nada que llevar ni recoger, asi que desde este
-- cambio ya no nace con fila en entregas_pedido (ver
-- PedidosCanaSvcImpl.crearEntregaInicial). Los que se crearon antes quedaron
-- con una entrega que no se podia avanzar: registrar un viaje exige items y
-- finalizar exige al menos un viaje.
--
-- Solo se borran entregas que nunca se movieron: sin viajes, sin constancia
-- firmada y sin finalizar. Si alguna tuvo movimiento se deja como esta.
--
-- Idempotente: correrlo de nuevo no borra nada mas.
-- Compatible con PostgreSQL 9.6.
--

SET search_path = cana, public;

-- Revision previa (opcional): que se va a borrar.
-- select en.id_entrega, en.correlativo_pedido
--   from cana.entregas_pedido en
--  where en.tipo_movimiento = 'ENT'
--    and coalesce(en.pedido_finalizado, false) = false
--    and not exists (select 1 from cana.detalle_pedido dp where dp.correlativo_pedido = en.correlativo_pedido)
--    and not exists (select 1 from cana.detalle_viaje dv where dv.id_entrega = en.id_entrega)
--    and not exists (select 1 from cana.documentos_entrega de where de.id_entrega = en.id_entrega);

DELETE FROM cana.entregas_pedido en
 WHERE en.tipo_movimiento = 'ENT'
   AND coalesce(en.pedido_finalizado, false) = false
   AND NOT EXISTS (SELECT 1 FROM cana.detalle_pedido dp
                    WHERE dp.correlativo_pedido = en.correlativo_pedido)
   AND NOT EXISTS (SELECT 1 FROM cana.detalle_viaje dv
                    WHERE dv.id_entrega = en.id_entrega)
   AND NOT EXISTS (SELECT 1 FROM cana.documentos_entrega de
                    WHERE de.id_entrega = en.id_entrega);
