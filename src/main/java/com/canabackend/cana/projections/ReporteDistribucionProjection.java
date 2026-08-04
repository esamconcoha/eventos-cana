package com.canabackend.cana.projections;

/** Una porcion de una grafica de composicion del reporte (tipo, estado, pago). */
public interface ReporteDistribucionProjection {
    String getEtiqueta();
    Long getCantidad();
    Double getMonto();
}
