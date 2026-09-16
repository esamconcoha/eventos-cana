package com.canabackend.cana.projections;

import java.time.LocalDateTime;

/** Linea de servicio de un pedido, con el nombre del servicio y el descuento resueltos. */
public interface DetalleServicioPedidoProjection {
    Long getIdDetalleServPedido();
    Long getIdServicio();
    String getNombreServicio();
    Double getCantidad();
    Double getPrecioAcordado();
    String getEspecificaciones();
    LocalDateTime getFechaRealizado();
    String getTipoDescuento();
    Double getValorDescuento();
    String getMotivoDescuento();
    /** Lo calcula cana.fn_descuento_linea, no el servicio. */
    Double getMontoDescuento();
    Double getSubtotalNeto();
}
