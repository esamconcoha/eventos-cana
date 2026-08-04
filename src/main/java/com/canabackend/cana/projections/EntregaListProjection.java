package com.canabackend.cana.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fila del listado de entregas, con cliente y estado del pedido resueltos. */
public interface EntregaListProjection {
    Long getIdEntrega();
    String getCorrelativoPedido();
    String getNombreClientePedido();
    String getDireccionPedido();
    Integer getCantidadViajesAproximados();
    Integer getCantidadViajesReales();
    Boolean getPedidoFinalizado();
    LocalDateTime getFechaInicioEntrega();
    LocalDateTime getFechaFinEntrega();
    LocalDateTime getFechaEvento();
    LocalDate getFechaEntrega();
    /** Null si el pedido no tiene un estado abierto (el join es LEFT). */
    Long getIdEstadoPedido();
    String getCodigoEstadoPedido();
    String getNombreEstadoPedido();
}
