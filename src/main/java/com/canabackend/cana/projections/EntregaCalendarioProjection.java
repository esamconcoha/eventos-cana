package com.canabackend.cana.projections;

import java.time.LocalDate;

/** Entrega o recoleccion ubicada en el calendario por su fecha pactada. */
public interface EntregaCalendarioProjection {
    Long getIdEntrega();
    String getCorrelativoPedido();
    LocalDate getFecha();
    String getNombreCliente();
    String getUbicacion();
    Integer getCantidadViajesReales();
    Integer getCantidadViajesAproximados();
    Boolean getEntregaFinalizada();
    String getCodigoEstadoPedido();
    String getNombreEstadoPedido();
}
