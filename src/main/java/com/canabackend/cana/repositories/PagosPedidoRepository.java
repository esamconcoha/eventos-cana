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
     * (detalle_servicio_pedido.cantidad x precio_acordado).
     */
    @Query(value =
            "select coalesce(sum(sub.subtotal), 0) from ( " +
            "  select (dp.cantidad_item_pedido * ic.costo_item) as subtotal " +
            "  from cana.detalle_pedido dp " +
            "  inner join cana.items_cana ic on ic.id_item = dp.id_item " +
            "  where dp.correlativo_pedido = :correlativoPedido " +
            "  union all " +
            "  select (dsp.cantidad * dsp.precio_acordado) as subtotal " +
            "  from cana.detalle_servicio_pedido dsp " +
            "  where dsp.correlativo_pedido = :correlativoPedido " +
            ") sub",
            nativeQuery = true)
    Double getTotalPedido(@Param("correlativoPedido") String correlativoPedido);
}
