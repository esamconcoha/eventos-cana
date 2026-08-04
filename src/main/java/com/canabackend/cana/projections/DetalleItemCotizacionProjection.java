package com.canabackend.cana.projections;

/** Linea de articulo de una cotizacion, con el nombre del item resuelto. */
public interface DetalleItemCotizacionProjection {
    Long getIdItem();
    String getNombreItem();
    Double getCostoItem();
    Double getCantidadItemCotizacion();
}
