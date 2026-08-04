package com.canabackend.cana.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pedido al que todavia se le puede abrir una entrega. */
public interface PedidoDisponibleProjection {
    String getCorrelativoPedido();
    String getNombreClientePedido();
    String getDireccionPedido();
    LocalDateTime getFechaEvento();
    LocalDate getFechaEntrega();
    Long getSalonEntrega();
    String getCodigoEstadoPedido();
    String getNombreEstadoPedido();
    Long getTotalItems();
}
