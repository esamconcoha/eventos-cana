package com.canabackend.cana.projections;

/** Los seis contadores del tablero de inicio en una sola consulta. */
public interface DashboardResumenProjection {
    Long getCotizacionesPendientes();
    Long getArticulosConFaltantes();
    Long getEventosProximos7Dias();
    Long getUsuariosActivos();
    Long getEntregasHoy();
    Long getRecoleccionesSinAgendar();
}
