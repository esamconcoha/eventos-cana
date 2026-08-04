package com.canabackend.cana.projections;

import java.time.LocalDateTime;

/** Pedido del periodo que todavia debe dinero. */
public interface ReporteCarteraProjection {
    String getCorrelativoPedido();
    String getCliente();
    LocalDateTime getFechaEvento();
    Double getTotal();
    Double getPagado();
    Double getSaldo();
    String getEstadoPago();
}
