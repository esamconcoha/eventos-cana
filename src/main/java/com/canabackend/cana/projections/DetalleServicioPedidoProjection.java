package com.canabackend.cana.projections;

import java.time.LocalDateTime;

/** Linea de servicio de un pedido, con el nombre del servicio resuelto. */
public interface DetalleServicioPedidoProjection {
    Long getIdDetalleServPedido();
    Long getIdServicio();
    String getNombreServicio();
    Double getCantidad();
    Double getPrecioAcordado();
    String getEspecificaciones();
    LocalDateTime getFechaRealizado();
}
