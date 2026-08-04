package com.canabackend.cana.projections;

/** Item de un viaje de entrega, con el nombre del articulo resuelto. */
public interface ViajeItemProjection {
    Long getIdDetalle();
    Long getIdViaje();
    Long getIdItem();
    String getNombreItem();
    Double getCantidadItem();
}
