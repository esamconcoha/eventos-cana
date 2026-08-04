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

    @Query(value =
            "select dp.id_item as idItem, ic.descripcion_item as nombreItem, " +
            "ic.costo_item as costoItem, dp.cantidad_item_pedido as cantidadItemPedido " +
            "from cana.detalle_pedido dp " +
            "inner join cana.items_cana ic on ic.id_item = dp.id_item " +
            "where dp.correlativo_pedido = :correlativoPedido", nativeQuery = true)
    List<DetalleItemPedidoProjection> findDetallesConNombre(@Param("correlativoPedido") String correlativoPedido);
}
