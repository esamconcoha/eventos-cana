package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetallePedido;
import com.canabackend.cana.projections.DetalleItemPedidoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByCorrelativoPedido(String correlativoPedido);

    void deleteByCorrelativoPedido(String correlativoPedido);

    /** Misma formula de descuento que el total del pedido; ver DetalleCotizacionRepository. */
    @Query(value =
            "select dp.id_item as idItem, ic.descripcion_item as nombreItem, " +
            "ic.costo_item as costoItem, dp.cantidad_item_pedido as cantidadItemPedido, " +
            "dp.tipo_descuento as tipoDescuento, " +
            "cast(dp.valor_descuento as double precision) as valorDescuento, " +
            "dp.motivo_descuento as motivoDescuento, " +
            "cast(cana.fn_descuento_linea(dp.cantidad_item_pedido * ic.costo_item, " +
            "     dp.tipo_descuento, dp.valor_descuento) as double precision) as montoDescuento, " +
            "cast(cana.fn_neto_linea(dp.cantidad_item_pedido * ic.costo_item, " +
            "     dp.tipo_descuento, dp.valor_descuento) as double precision) as subtotalNeto " +
            "from cana.detalle_pedido dp " +
            "inner join cana.items_cana ic on ic.id_item = dp.id_item " +
            "where dp.correlativo_pedido = :correlativoPedido", nativeQuery = true)
    List<DetalleItemPedidoProjection> findDetallesConNombre(@Param("correlativoPedido") String correlativoPedido);
}
