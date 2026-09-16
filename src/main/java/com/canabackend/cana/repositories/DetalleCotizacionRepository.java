package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetalleCotizacion;
import com.canabackend.cana.projections.DetalleItemCotizacionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Long> {

    List<DetalleCotizacion> findByIdCotizacion(Long idCotizacion);

    void deleteByIdCotizacion(Long idCotizacion);

    /**
     * El descuento se resuelve con cana.fn_descuento_linea y no en Java para que
     * la linea muestre exactamente lo mismo que suma el total del pedido, que
     * tambien sale de esa funcion (ver PagosPedidoRepository.getTotalPedido).
     */
    @Query(value =
            "select dc.id_item as idItem, ic.descripcion_item as nombreItem, " +
            "ic.costo_item as costoItem, dc.cantidad_item_cotizacion as cantidadItemCotizacion, " +
            "dc.tipo_descuento as tipoDescuento, " +
            "cast(dc.valor_descuento as double precision) as valorDescuento, " +
            "dc.motivo_descuento as motivoDescuento, " +
            "cast(cana.fn_descuento_linea(dc.cantidad_item_cotizacion * ic.costo_item, " +
            "     dc.tipo_descuento, dc.valor_descuento) as double precision) as montoDescuento, " +
            "cast(cana.fn_neto_linea(dc.cantidad_item_cotizacion * ic.costo_item, " +
            "     dc.tipo_descuento, dc.valor_descuento) as double precision) as subtotalNeto " +
            "from cana.detalle_cotizacion dc " +
            "inner join cana.items_cana ic on ic.id_item = dc.id_item " +
            "where dc.id_cotizacion = :idCotizacion", nativeQuery = true)
    List<DetalleItemCotizacionProjection> findDetallesConNombre(@Param("idCotizacion") Long idCotizacion);
}
