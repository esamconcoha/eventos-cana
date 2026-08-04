package com.canabackend.cana.projections;

/** Cliente del ranking de facturacion del periodo. */
public interface ReporteClienteProjection {
    String getNombre();
    Long getEventos();
    Double getFacturado();
    Double getCobrado();
    Double getSaldo();
}
