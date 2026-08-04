package com.canabackend.cana.projections;

/** Contadores del tablero de entregas/recolecciones en una sola consulta. */
public interface EstadisticasProjection {
    Long getEnCurso();
    Long getProgramadas();
    Long getAtrasadas();
    Long getSinFecha();
    Long getFinalizadas();
    Long getViajesHoy();
    Long getDesvioViajes();
}
