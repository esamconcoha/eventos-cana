package com.canabackend.cana.repositories;

import com.canabackend.cana.models.PagosPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagosPedidoRepository extends JpaRepository<PagosPedido, Long> {

    List<PagosPedido> findByCorrelativoPedidoOrderByFechaPagoDesc(String correlativoPedido);

    /**
     * Total del pedido: items (detalle_pedido x items_cana.costo_item) + servicios
     * (detalle_servicio_pedido.cantidad x precio_acordado), cada linea NETA de su
     * descuento.
     *
     * <p>El descuento se aplica linea por linea con cana.fn_neto_linea y no como
     * un porcentaje al final: una linea exonerada (EXO) tiene que salir en Q 0.00
     * aunque el resto se cobre completo, y eso solo cierra por linea.
     */
    @Query(value =
            "select coalesce(sum(sub.subtotal), 0) from ( " +
            "  select cana.fn_neto_linea(dp.cantidad_item_pedido * ic.costo_item, " +
            "         dp.tipo_descuento, dp.valor_descuento) as subtotal " +
            "  from cana.detalle_pedido dp " +
            "  inner join cana.items_cana ic on ic.id_item = dp.id_item " +
            "  where dp.correlativo_pedido = :correlativoPedido " +
            "  union all " +
            "  select cana.fn_neto_linea(dsp.cantidad * dsp.precio_acordado, " +
            "         dsp.tipo_descuento, dsp.valor_descuento) as subtotal " +
            "  from cana.detalle_servicio_pedido dsp " +
            "  where dsp.correlativo_pedido = :correlativoPedido " +
            ") sub",
            nativeQuery = true)
    Double getTotalPedido(@Param("correlativoPedido") String correlativoPedido);

    /**
     * Lo que se descuenta en total en el pedido (articulos + servicios). Solo
     * informativo: el que manda sobre el saldo es {@link #getTotalPedido}.
     */
    @Query(value =
            "select coalesce(sum(sub.descuento), 0) from ( " +
            "  select cana.fn_descuento_linea(dp.cantidad_item_pedido * ic.costo_item, " +
            "         dp.tipo_descuento, dp.valor_descuento) as descuento " +
            "  from cana.detalle_pedido dp " +
            "  inner join cana.items_cana ic on ic.id_item = dp.id_item " +
            "  where dp.correlativo_pedido = :correlativoPedido " +
            "  union all " +
            "  select cana.fn_descuento_linea(dsp.cantidad * dsp.precio_acordado, " +
            "         dsp.tipo_descuento, dsp.valor_descuento) as descuento " +
            "  from cana.detalle_servicio_pedido dsp " +
            "  where dsp.correlativo_pedido = :correlativoPedido " +
            ") sub",
            nativeQuery = true)
    Double getTotalDescuentoPedido(@Param("correlativoPedido") String correlativoPedido);
}
