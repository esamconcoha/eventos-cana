package com.canabackend.cana.repositories;

import com.canabackend.cana.models.EntregasPedido;
import com.canabackend.cana.projections.EntregaCalendarioProjection;
import com.canabackend.cana.projections.EntregaListProjection;
import com.canabackend.cana.projections.EstadisticasProjection;
import com.canabackend.cana.projections.ItemEntregaProjection;
import com.canabackend.cana.projections.ItemRecoleccionProjection;
import com.canabackend.cana.projections.PedidoDisponibleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface EntregasPedidoRepository extends JpaRepository<EntregasPedido,Long> {

    boolean existsByCorrelativoPedidoAndTipoMovimiento(String correlativoPedido, String tipoMovimiento);

    Optional<EntregasPedido> findByCorrelativoPedidoAndTipoMovimiento(String correlativoPedido,
                                                                      String tipoMovimiento);

    /**
     * El nombre del cliente se resuelve igual que en PedidosCanaSvcImpl: si el
     * pedido esta ligado a una cuenta registrada se arma desde usuarios, y si no
     * se usa el nombre suelto del pedido directo.
     */
    @Query(value =
            "select en.id_entrega as idEntrega, en.correlativo_pedido as correlativoPedido, " +
            "coalesce(u.nombres_usuario || ' ' || u.apellidos_usuario, p.nombre_cliente_pedidoprov) as nombreClientePedido, " +
            "p.direccion_pedido as direccionPedido, " +
            "en.cantidad_viajes_aproximados as cantidadViajesAproximados, " +
            "coalesce(en.cantidad_viajes_reales, 0) as cantidadViajesReales, " +
            "coalesce(en.pedido_finalizado, false) as pedidoFinalizado, " +
            "en.fecha_inicio_entrega as fechaInicioEntrega, en.fecha_fin_entrega as fechaFinEntrega, " +
            "p.fecha_evento as fechaEvento, " +
            // fecha de agenda del movimiento: la entrega se pacta con
            // fecha_entrega y la recoleccion con fecha_recogido
            "case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end as fechaEntrega, " +
            "e.id_estado as idEstadoPedido, e.codigo_estado as codigoEstadoPedido, " +
            "e.nombre_estado as nombreEstadoPedido " +
            "from cana.entregas_pedido en " +
            "inner join cana.pedidos_cana p on p.correlativo_pedido = en.correlativo_pedido " +
            "left join cana.usuarios u on u.dpi_nit_usuario = p.dpi_usuario_pedido " +
            "left join cana.estados_pedido ep on ep.correlativo_pedido = p.correlativo_pedido " +
            "     and ep.fecha_hora_fin is null " +
            "left join cana.estados e on e.id_estado = ep.id_estado " +
            "where en.tipo_movimiento = :tipoMovimiento " +
            // un pedido cancelado sale de circulacion: no se lista ni cuenta en
            // ningun tablero, aunque su fila en entregas_pedido siga existiendo
            "  and p.estado_pedido is distinct from 'ECA' " +
            "order by coalesce(en.pedido_finalizado, false), en.id_entrega desc", nativeQuery = true)
    List<EntregaListProjection> findEntregasConCliente(@Param("tipoMovimiento") String tipoMovimiento);

    /**
     * Todos los contadores del tablero en una sola ida a la BD.
     *
     * Las entregas no finalizadas se parten por la fecha de entrega pactada del
     * pedido (hoy / futura / vencida / sin fecha), asi que los cuatro grupos son
     * disjuntos y suman el total de entregas abiertas.
     *
     * No hace falta filtrar ademas por "tiene viajes propuestos": desde que
     * cantidad_viajes_aproximados es obligatorio al crear el pedido siempre es
     * >= 1, y las filas viejas que podrian no tenerlo son las mismas que caen
     * en sinFecha.
     *
     * desvioViajes se mide solo sobre entregas finalizadas: en las abiertas los
     * viajes reales aun estan creciendo y el desvio siempre daria negativo.
     */
    @Query(value =
            "select " +
            " count(*) filter (where coalesce(en.pedido_finalizado, false) = false " +
            "                    and case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end = :hoy) as enCurso, " +
            " count(*) filter (where coalesce(en.pedido_finalizado, false) = false " +
            "                    and case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end > :hoy) as programadas, " +
            " count(*) filter (where coalesce(en.pedido_finalizado, false) = false " +
            "                    and case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end < :hoy) as atrasadas, " +
            " count(*) filter (where coalesce(en.pedido_finalizado, false) = false " +
            "                    and case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end is null) as sinFecha, " +
            " count(*) filter (where coalesce(en.pedido_finalizado, false) = true) as finalizadas, " +
            " coalesce(sum(coalesce(en.cantidad_viajes_reales, 0) " +
            "            - coalesce(en.cantidad_viajes_aproximados, 0)) " +
            "          filter (where coalesce(en.pedido_finalizado, false) = true), 0) as desvioViajes, " +
            // el join a entregas_pedido no es decorativo: sin el, los viajes de
            // recoleccion se contarian como viajes de entrega
            " (select count(*) from cana.detalle_viaje dv " +
            "   inner join cana.entregas_pedido en2 on en2.id_entrega = dv.id_entrega " +
            "   inner join cana.pedidos_cana p2 on p2.correlativo_pedido = en2.correlativo_pedido " +
            "   where en2.tipo_movimiento = :tipoMovimiento " +
            "     and p2.estado_pedido is distinct from 'ECA' " +
            "     and dv.fecha_inicio_viaje >= :desde and dv.fecha_inicio_viaje < :hasta) as viajesHoy " +
            "from cana.entregas_pedido en " +
            "inner join cana.pedidos_cana p on p.correlativo_pedido = en.correlativo_pedido " +
            // los pedidos cancelados no cuentan en ningun contador del tablero
            "where en.tipo_movimiento = :tipoMovimiento " +
            "  and p.estado_pedido is distinct from 'ECA'",
            nativeQuery = true)
    EstadisticasProjection obtenerEstadisticas(@Param("tipoMovimiento") String tipoMovimiento,
                                               @Param("hoy") LocalDate hoy,
                                               @Param("desde") LocalDateTime desde,
                                               @Param("hasta") LocalDateTime hasta);

    /**
     * Pedidos a los que todavia se les puede abrir una entrega.
     *
     * Criterio: pedido activo, estado actual del ciclo de vida anterior a la
     * entrega, y sin fila en entregas_pedido. NO se filtra por fecha_entrega:
     * esa es la fecha pactada con el cliente que se captura al crear el pedido,
     * y viene null solo en los pedidos nacidos de una cotizacion.
     *
     * El filtro de tipo_estado acepta 'EVENTO' ademas de 'EVE' por el dato
     * inconsistente documentado en sql/002 (corregido en sql/005; el OR queda
     * como red de seguridad si esa migracion no se corrio).
     */
    @Query(value =
            "select p.correlativo_pedido as correlativoPedido, " +
            "coalesce(u.nombres_usuario || ' ' || u.apellidos_usuario, p.nombre_cliente_pedidoprov) as nombreClientePedido, " +
            "p.direccion_pedido as direccionPedido, p.fecha_evento as fechaEvento, " +
            "p.fecha_entrega as fechaEntrega, p.salon_entrega as salonEntrega, " +
            "e.codigo_estado as codigoEstadoPedido, e.nombre_estado as nombreEstadoPedido, " +
            "(select count(*) from cana.detalle_pedido dp " +
            "  where dp.correlativo_pedido = p.correlativo_pedido) as totalItems " +
            "from cana.pedidos_cana p " +
            "left join cana.usuarios u on u.dpi_nit_usuario = p.dpi_usuario_pedido " +
            "inner join cana.estados_pedido ep on ep.correlativo_pedido = p.correlativo_pedido " +
            "     and ep.fecha_hora_fin is null " +
            "inner join cana.estados e on e.id_estado = ep.id_estado " +
            "where p.estado_pedido is distinct from 'ECA' " +
            "  and e.tipo_estado in ('EVE', 'EVENTO') " +
            "  and e.codigo_estado in (:codigosEstado) " +
            "  and not exists (select 1 from cana.entregas_pedido en " +
            "                   where en.correlativo_pedido = p.correlativo_pedido " +
            "                     and en.tipo_movimiento = :tipoMovimiento) " +
            "order by p.fecha_evento nulls last, p.correlativo_pedido", nativeQuery = true)
    List<PedidoDisponibleProjection> findPedidosDisponibles(@Param("codigosEstado") List<String> codigosEstado,
                                                            @Param("tipoMovimiento") String tipoMovimiento);

    /**
     * Avance de despacho item por item del pedido de esta entrega: lo pedido
     * contra lo que ya viajo. El pendiente se calcula en el servicio.
     *
     * detalle_pedido no tiene unique por (correlativo, item), asi que se agrupa
     * por item para no duplicar filas si el mismo item aparece dos veces.
     */
    @Query(value =
            "select dp.id_item as idItem, ic.descripcion_item as nombreItem, " +
            // ambos son numeric en BD; se castean para que la proyeccion reciba Double
            "sum(dp.cantidad_item_pedido)::double precision as cantidadPedida, " +
            "coalesce(max(env.enviado), 0)::double precision as cantidadEnviada " +
            "from cana.entregas_pedido en " +
            "inner join cana.detalle_pedido dp on dp.correlativo_pedido = en.correlativo_pedido " +
            "inner join cana.items_cana ic on ic.id_item = dp.id_item " +
            "left join ( " +
            "   select dvi.id_item as id_item, sum(dvi.cantidad_item) as enviado " +
            "   from cana.detalle_viaje_items dvi " +
            "   inner join cana.detalle_viaje dv on dv.id_viaje = dvi.id_detalle_viaje " +
            "   where dv.id_entrega = :idEntrega " +
            "   group by dvi.id_item " +
            ") env on env.id_item = dp.id_item " +
            "where en.id_entrega = :idEntrega " +
            "group by dp.id_item, ic.descripcion_item " +
            "order by ic.descripcion_item", nativeQuery = true)
    List<ItemEntregaProjection> findItemsEntrega(@Param("idEntrega") Long idEntrega);

    /**
     * Entregas del calendario, ubicadas por la fecha PACTADA con el cliente
     * (pedidos_cana.fecha_entrega), que es la que se agenda. Las fechas de
     * ejecucion (fecha_inicio_entrega, viajes) no mueven la entrada de dia.
     */
    @Query(value =
            "select en.id_entrega as idEntrega, en.correlativo_pedido as correlativoPedido, " +
            "case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end as fecha, " +
            "coalesce(u.nombres_usuario || ' ' || u.apellidos_usuario, p.nombre_cliente_pedidoprov) as nombreCliente, " +
            "p.direccion_pedido as ubicacion, " +
            "coalesce(en.cantidad_viajes_reales, 0) as cantidadViajesReales, " +
            "en.cantidad_viajes_aproximados as cantidadViajesAproximados, " +
            "coalesce(en.pedido_finalizado, false) as entregaFinalizada, " +
            "e.codigo_estado as codigoEstadoPedido, e.nombre_estado as nombreEstadoPedido " +
            "from cana.entregas_pedido en " +
            "inner join cana.pedidos_cana p on p.correlativo_pedido = en.correlativo_pedido " +
            "left join cana.usuarios u on u.dpi_nit_usuario = p.dpi_usuario_pedido " +
            "left join cana.estados_pedido ep on ep.correlativo_pedido = p.correlativo_pedido " +
            "     and ep.fecha_hora_fin is null " +
            "left join cana.estados e on e.id_estado = ep.id_estado " +
            "where p.estado_pedido is distinct from 'ECA' " +
            "  and en.tipo_movimiento = :tipoMovimiento " +
            "  and case when en.tipo_movimiento = 'REC' then p.fecha_recogido else p.fecha_entrega end between :desde and :hasta " +
            "order by 3, en.id_entrega", nativeQuery = true)
    List<EntregaCalendarioProjection> findEntregasEntre(@Param("tipoMovimiento") String tipoMovimiento,
                                                        @Param("desde") LocalDate desde,
                                                        @Param("hasta") LocalDate hasta);

    /**
     * Avance de una recoleccion item por item.
     *
     * Aca esta la unica asimetria real con la entrega: el tope no es lo pedido
     * (detalle_pedido) sino lo que efectivamente SALIO en los viajes de ida. No
     * se puede traer de vuelta algo que nunca se entrego, y si se entregaron 90
     * de 100 sillas, el maximo a recolectar es 90.
     *
     * Lo que quede pendiente al cerrar la recoleccion es, por definicion, el
     * faltante: no hace falta guardarlo, se deriva de estas mismas sumas.
     */
    @Query(value =
            "with movimientos as ( " +
            "  select rec.id_entrega as id_rec, ent.id_entrega as id_ent " +
            "  from cana.entregas_pedido rec " +
            "  inner join cana.entregas_pedido ent " +
            "          on ent.correlativo_pedido = rec.correlativo_pedido " +
            "         and ent.tipo_movimiento = 'ENT' " +
            "  where rec.id_entrega = :idRecoleccion " +
            "), totales as ( " +
            "  select dv.id_entrega, dvi.id_item, sum(dvi.cantidad_item) as cantidad " +
            "  from cana.detalle_viaje_items dvi " +
            "  inner join cana.detalle_viaje dv on dv.id_viaje = dvi.id_detalle_viaje " +
            "  where dv.id_entrega in (select id_rec from movimientos " +
            "                          union all select id_ent from movimientos) " +
            "  group by dv.id_entrega, dvi.id_item " +
            ") " +
            "select e.id_item as idItem, ic.descripcion_item as nombreItem, " +
            "e.cantidad::double precision as cantidadEntregada, " +
            "coalesce(r.cantidad, 0)::double precision as cantidadRecolectada " +
            "from movimientos m " +
            "inner join totales e on e.id_entrega = m.id_ent " +
            "inner join cana.items_cana ic on ic.id_item = e.id_item " +
            "left join totales r on r.id_entrega = m.id_rec and r.id_item = e.id_item " +
            "order by ic.descripcion_item", nativeQuery = true)
    List<ItemRecoleccionProjection> findItemsRecoleccion(@Param("idRecoleccion") Long idRecoleccion);
}
