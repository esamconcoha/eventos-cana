package com.canabackend.cana.projections;

import java.time.LocalDateTime;

/** Un tramo del historial de estados de una cotizacion, con su nombre resuelto. */
public interface HistorialEstadoCotizacionProjection {
    Long getIdEstado();
    String getCodigoEstado();
    String getNombreEstado();
    LocalDateTime getFechaHoraInicio();
    LocalDateTime getFechaHoraFin();
}
