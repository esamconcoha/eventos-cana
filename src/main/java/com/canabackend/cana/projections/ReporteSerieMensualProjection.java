package com.canabackend.cana.projections;

/** Un punto de la serie mensual del reporte. {@code periodo} viene como "yyyy-MM". */
public interface ReporteSerieMensualProjection {
    String getPeriodo();
    Long getEventos();
    Double getFacturado();
    Double getCobrado();
}
