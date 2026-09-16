package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetalleServicioPedido;
import com.canabackend.cana.projections.DetalleServicioPedidoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleServicioPedidoRepository extends JpaRepository<DetalleServicioPedido, Long> {

    List<DetalleServicioPedido> findByCorrelativoPedido(String correlativoPedido);

    void deleteByCorrelativoPedido(String correlativoPedido);

    /** Misma formula de descuento que el total del pedido; ver DetalleCotizacionRepository. */
    @Query(value =
            "select dsp.id_detalle_serv_pedido as idDetalleServPedido, " +
            "dsp.id_servicio as idServicio, sd.nombre_servicio as nombreServicio, " +
            "dsp.cantidad as cantidad, dsp.precio_acordado as precioAcordado, " +
            "dsp.especificaciones as especificaciones, dsp.fecha_realizado as fechaRealizado, " +
            "dsp.tipo_descuento as tipoDescuento, " +
            "cast(dsp.valor_descuento as double precision) as valorDescuento, " +
            "dsp.motivo_descuento as motivoDescuento, " +
            "cast(cana.fn_descuento_linea(dsp.cantidad * dsp.precio_acordado, " +
            "     dsp.tipo_descuento, dsp.valor_descuento) as double precision) as montoDescuento, " +
            "cast(cana.fn_neto_linea(dsp.cantidad * dsp.precio_acordado, " +
            "     dsp.tipo_descuento, dsp.valor_descuento) as double precision) as subtotalNeto " +
            "from cana.detalle_servicio_pedido dsp " +
            "inner join cana.servicios_decoracion sd on sd.id_servicio = dsp.id_servicio " +
            "where dsp.correlativo_pedido = :correlativoPedido", nativeQuery = true)
    List<DetalleServicioPedidoProjection> findDetallesConNombre(@Param("correlativoPedido") String correlativoPedido);
}
