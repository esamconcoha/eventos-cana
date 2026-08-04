package com.canabackend.cana.projections;

import java.time.LocalDateTime;

/** Evento del calendario ubicado por la fecha del evento en si. */
public interface EventoCalendarioProjection {
    String getCorrelativoPedido();
    LocalDateTime getFechaHora();
    String getNombreCliente();
    String getUbicacion();
    String getNombreTipoEvento();
    String getCodigoEstadoPedido();
    String getNombreEstadoPedido();
}
