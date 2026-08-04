package com.canabackend.cana.projections;

/** Linea de un top del reporte: articulos, servicios o faltantes de bodega. */
public interface ReporteRankingProjection {
    String getEtiqueta();
    String getCategoria();
    Double getCantidad();
    Double getMonto();
    Long getEventos();
    Long getDisponible();
}
