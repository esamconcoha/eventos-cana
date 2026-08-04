package com.canabackend.cana.projections;

/** Avance de una recoleccion item por item: lo entregado contra lo recolectado. */
public interface ItemRecoleccionProjection {
    Long getIdItem();
    String getNombreItem();
    Double getCantidadEntregada();
    Double getCantidadRecolectada();
}
