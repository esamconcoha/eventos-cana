package com.canabackend.cana.repositories;

import com.canabackend.cana.models.PedidosCana;
import com.canabackend.cana.projections.ReporteCarteraProjection;
import com.canabackend.cana.projections.ReporteClienteProjection;
import com.canabackend.cana.projections.ReporteDistribucionProjection;
import com.canabackend.cana.projections.ReporteRankingProjection;
import com.canabackend.cana.projections.ReporteResumenProjection;
import com.canabackend.cana.projections.ReporteSerieMensualProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio solo-lectura del modulo de reporteria. Igual que
 * {@link DashboardRepository}, cruza tablas de agregados distintos (pedidos,
 * pagos, detalle, catalogos), asi que extiende {@code Repository} y no expone CRUD.
 *
 * <p><b>Criterio de periodo</b>: todas las consultas parten de los pedidos cuya
 * {@code fecha_evento} cae en el rango. El dinero cobrado es el de esos mismos
 * pedidos sin importar cuando entro el pago; asi facturado, cobrado y saldo
 * siempre cuadran entre si (cobrado por fecha de pago daria un saldo que no es
 * la resta de las otras dos cifras del reporte).
 *
 * <p><b>Criterio de monto</b>: el total del pedido se calcula igual que en
 * {@link PagosPedidoRepository#getTotalPedido} (items x costo_item + servicios x
 * precio_acordado). Si las dos formulas se separan, el saldo del reporte
 * discreparia del estado de cuenta de cada pedido.
 */
public interface ReportesRepository extends Repository<PedidosCana, String> {

    /**
     * Base comun: pedidos del rango con su cliente resuelto, su total y lo
     * cobrado neto de devoluciones. Se concatena en cada consulta porque las
     * anotaciones solo admiten constantes de compilacion.
     */
    String CTE_PEDIDOS =
            "with ped as ( " +
            "  select p.correlativo_pedido, p.estado_pedido, p.estado_pago, p.fecha_evento, " +
            "         p.cod_tipo_evento, " +
            "         coalesce(nullif(trim(u.nombres_usuario || ' ' || u.apellidos_usuario), ''), " +
            "                  nullif(trim(p.nombre_cliente_pedidoprov), ''), 'Sin cliente') as cliente " +
            "  from cana.pedidos_cana p " +
            "  left join cana.usuarios u on u.dpi_nit_usuario = p.dpi_usuario_pedido " +
            "  where p.fecha_evento >= :desde and p.fecha_evento < :hasta " +
            "), " +
            "tot as ( " +
            "  select pe.*, " +
            "    coalesce((select sum(dp.cantidad_item_pedido * ic.costo_item) " +
            "                from cana.detalle_pedido dp " +
            "                join cana.items_cana ic on ic.id_item = dp.id_item " +
            "               where dp.correlativo_pedido = pe.correlativo_pedido), 0) " +
            "  + coalesce((select sum(dsp.cantidad * dsp.precio_acordado) " +
            "                from cana.detalle_servicio_pedido dsp " +
            "               where dsp.correlativo_pedido = pe.correlativo_pedido), 0) as total, " +
            "    coalesce((select sum(case when pp.tipo_pago = 'DEVOLUCION' then -pp.monto_pago " +
            "                              else pp.monto_pago end) " +
            "                from cana.pagos_pedido pp " +
            "               where pp.correlativo_pedido = pe.correlativo_pedido " +
            "                 and pp.estado_registro is true), 0) as pagado " +
            "  from ped pe " +
            ") ";

    /** Los pedidos cancelados no facturan: se cuentan aparte, nunca suman dinero. */
    String NO_CANCELADO = " estado_pedido is distinct from 'ECA' ";

    // ─────────────────────────────────────────────────────────────
    // Resumen ejecutivo
    // ─────────────────────────────────────────────────────────────

    @Query(value = CTE_PEDIDOS +
            "select " +
            " (select count(*) from tot where " + NO_CANCELADO + ") as eventos, " +
            " (select count(*) from tot where estado_pedido = 'ECA') as eventosCancelados, " +
            " (select count(*) from tot where estado_pedido = 'FIN') as eventosFinalizados, " +
            " (select coalesce(sum(total), 0) from tot where " + NO_CANCELADO + ") as facturado, " +
            " (select coalesce(sum(pagado), 0) from tot where " + NO_CANCELADO + ") as cobrado, " +
            " (select count(*) from tot where " + NO_CANCELADO + " and total - pagado > 0.009) as pedidosConSaldo, " +
            " (select count(*) from cana.cotizaciones c " +
            "   where c.fecha_cotizacion >= :desdeFecha and c.fecha_cotizacion <= :hastaFecha) as cotizaciones, " +
            " (select count(*) from cana.cotizaciones c " +
            "   where c.fecha_cotizacion >= :desdeFecha and c.fecha_cotizacion <= :hastaFecha " +
            "     and c.estado_cotizacion = 'CONF') as cotizacionesConfirmadas, " +
            " (select count(*) from cana.items_cana " +
            "   where estado_item is true and coalesce(cantidad_faltantes, 0) > 0) as articulosConFaltantes, " +
            " (select coalesce(sum(cantidad_faltantes), 0) from cana.items_cana " +
            "   where estado_item is true) as unidadesFaltantes",
            nativeQuery = true)
    ReporteResumenProjection obtenerResumen(@Param("desde") LocalDateTime desde,
                                            @Param("hasta") LocalDateTime hasta,
                                            @Param("desdeFecha") LocalDate desdeFecha,
                                            @Param("hastaFecha") LocalDate hastaFecha);

    // ─────────────────────────────────────────────────────────────
    // Series de tiempo
    // ─────────────────────────────────────────────────────────────

    /** Un punto por mes con evento; los meses vacios los rellena el servicio. */
    @Query(value = CTE_PEDIDOS +
            "select to_char(date_trunc('month', fecha_evento), 'YYYY-MM') as periodo, " +
            "       count(*) as eventos, " +
            "       coalesce(sum(total), 0) as facturado, " +
            "       coalesce(sum(pagado), 0) as cobrado " +
            "  from tot where " + NO_CANCELADO +
            " group by 1 order by 1",
            nativeQuery = true)
    List<ReporteSerieMensualProjection> serieMensual(@Param("desde") LocalDateTime desde,
                                                     @Param("hasta") LocalDateTime hasta);

    // ─────────────────────────────────────────────────────────────
    // Distribuciones (gráficas de composición)
    // ─────────────────────────────────────────────────────────────

    /**
     * El nombre del tipo de evento sale por subconsulta con limit 1 y no por
     * join: catalogos_cana.codigo no es unico entre tipos de catalogo.
     */
    @Query(value = CTE_PEDIDOS +
            "select coalesce((select cc.nombre from cana.catalogos_cana cc " +
            "                  where cc.codigo = t.cod_tipo_evento limit 1), 'Sin tipo') as etiqueta, " +
            "       count(*) as cantidad, " +
            "       coalesce(sum(t.total), 0) as monto " +
            "  from tot t where " + NO_CANCELADO +
            " group by 1 order by 2 desc, 1",
            nativeQuery = true)
    List<ReporteDistribucionProjection> eventosPorTipo(@Param("desde") LocalDateTime desde,
                                                       @Param("hasta") LocalDateTime hasta);

    /** Composicion de la cartera: cuanto dinero hay en cada estado de pago. */
    @Query(value = CTE_PEDIDOS +
            "select coalesce(e.nombre_estado, t.estado_pago) as etiqueta, " +
            "       count(*) as cantidad, " +
            "       coalesce(sum(t.total - t.pagado), 0) as monto " +
            "  from tot t " +
            "  left join cana.estados e on e.tipo_estado = 'PAGO' and e.codigo_estado = t.estado_pago " +
            " where " + NO_CANCELADO +
            " group by 1 order by 2 desc",
            nativeQuery = true)
    List<ReporteDistribucionProjection> carteraPorEstadoPago(@Param("desde") LocalDateTime desde,
                                                             @Param("hasta") LocalDateTime hasta);

    /** En que punto del ciclo logistico estan los eventos del periodo. */
    @Query(value = CTE_PEDIDOS +
            "select coalesce(e.nombre_estado, t.estado_pedido) as etiqueta, " +
            "       count(*) as cantidad, " +
            "       coalesce(sum(t.total), 0) as monto " +
            "  from tot t " +
            "  left join cana.estados e on e.tipo_estado = 'EVE' and e.codigo_estado = t.estado_pedido " +
            " group by 1 order by 2 desc",
            nativeQuery = true)
    List<ReporteDistribucionProjection> eventosPorEstado(@Param("desde") LocalDateTime desde,
                                                         @Param("hasta") LocalDateTime hasta);

    // ─────────────────────────────────────────────────────────────
    // Rankings
    // ─────────────────────────────────────────────────────────────

    /**
     * Articulos mas rentados. {@code disponible} es el stock declarado, para leer
     * la demanda contra lo que hay en bodega.
     */
    @Query(value = CTE_PEDIDOS +
            "select ic.descripcion_item as etiqueta, " +
            "       coalesce(cc.nombre, 'Sin categoria') as categoria, " +
            "       sum(dp.cantidad_item_pedido) as cantidad, " +
            "       sum(dp.cantidad_item_pedido * ic.costo_item) as monto, " +
            "       count(distinct t.correlativo_pedido) as eventos, " +
            "       max(ic.cantidad_item) as disponible " +
            "  from tot t " +
            "  join cana.detalle_pedido dp on dp.correlativo_pedido = t.correlativo_pedido " +
            "  join cana.items_cana ic on ic.id_item = dp.id_item " +
            "  left join cana.catalogos_cana cc on cc.id_catalogo = ic.id_tipo_item and cc.id_tipo_catalogo = 2 " +
            " where " + NO_CANCELADO +
            " group by ic.id_item, ic.descripcion_item, cc.nombre " +
            " order by cantidad desc limit :limite",
            nativeQuery = true)
    List<ReporteRankingProjection> topArticulos(@Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDateTime hasta,
                                                @Param("limite") int limite);

    /** Servicios mas vendidos, ordenados por lo que dejan (no por cuantas veces). */
    @Query(value = CTE_PEDIDOS +
            "select sd.nombre_servicio as etiqueta, " +
            "       coalesce(cs.nombre_categoria, 'Sin categoria') as categoria, " +
            "       sum(dsp.cantidad) as cantidad, " +
            "       sum(dsp.cantidad * dsp.precio_acordado) as monto, " +
            "       count(distinct t.correlativo_pedido) as eventos, " +
            "       cast(null as bigint) as disponible " +
            "  from tot t " +
            "  join cana.detalle_servicio_pedido dsp on dsp.correlativo_pedido = t.correlativo_pedido " +
            "  join cana.servicios_decoracion sd on sd.id_servicio = dsp.id_servicio " +
            "  left join cana.categorias_servicio cs on cs.id_categoria = sd.id_categoria " +
            " where " + NO_CANCELADO +
            " group by sd.id_servicio, sd.nombre_servicio, cs.nombre_categoria " +
            " order by monto desc limit :limite",
            nativeQuery = true)
    List<ReporteRankingProjection> topServicios(@Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDateTime hasta,
                                                @Param("limite") int limite);

    /** Clientes que mas facturan en el periodo, con lo que aun deben. */
    @Query(value = CTE_PEDIDOS +
            "select cliente as nombre, " +
            "       count(*) as eventos, " +
            "       coalesce(sum(total), 0) as facturado, " +
            "       coalesce(sum(pagado), 0) as cobrado, " +
            "       coalesce(sum(total - pagado), 0) as saldo " +
            "  from tot where " + NO_CANCELADO +
            " group by 1 order by facturado desc limit :limite",
            nativeQuery = true)
    List<ReporteClienteProjection> topClientes(@Param("desde") LocalDateTime desde,
                                               @Param("hasta") LocalDateTime hasta,
                                               @Param("limite") int limite);

    // ─────────────────────────────────────────────────────────────
    // Detalle operativo
    // ─────────────────────────────────────────────────────────────

    /** Pedidos con saldo abierto, del que mas debe al que menos. */
    @Query(value = CTE_PEDIDOS +
            "select t.correlativo_pedido as correlativoPedido, " +
            "       t.cliente as cliente, " +
            "       t.fecha_evento as fechaEvento, " +
            "       t.total as total, " +
            "       t.pagado as pagado, " +
            "       t.total - t.pagado as saldo, " +
            "       coalesce(e.nombre_estado, t.estado_pago) as estadoPago " +
            "  from tot t " +
            "  left join cana.estados e on e.tipo_estado = 'PAGO' and e.codigo_estado = t.estado_pago " +
            " where " + NO_CANCELADO + " and t.total - t.pagado > 0.009 " +
            " order by saldo desc limit :limite",
            nativeQuery = true)
    List<ReporteCarteraProjection> pedidosConSaldo(@Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta,
                                                   @Param("limite") int limite);

    /**
     * Faltantes de inventario. Es una foto del estado actual de bodega, no del
     * periodo: items_cana.cantidad_faltantes no guarda cuando se registro cada
     * unidad perdida, asi que filtrarlo por fecha seria inventar el dato.
     */
    @Query(value =
            "select ic.descripcion_item as etiqueta, " +
            "       coalesce(cc.nombre, 'Sin categoria') as categoria, " +
            "       cast(ic.cantidad_faltantes as double precision) as cantidad, " +
            "       cast(coalesce(ic.cantidad_faltantes, 0) * coalesce(ic.costo_item, 0) as double precision) as monto, " +
            "       cast(null as bigint) as eventos, " +
            "       ic.cantidad_item as disponible " +
            "  from cana.items_cana ic " +
            "  left join cana.catalogos_cana cc on cc.id_catalogo = ic.id_tipo_item and cc.id_tipo_catalogo = 2 " +
            " where ic.estado_item is true and coalesce(ic.cantidad_faltantes, 0) > 0 " +
            " order by ic.cantidad_faltantes desc limit :limite",
            nativeQuery = true)
    List<ReporteRankingProjection> articulosConFaltantes(@Param("limite") int limite);
}
