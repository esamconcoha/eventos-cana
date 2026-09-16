package com.canabackend.cana.projections;

/** Linea de articulo de una cotizacion, con el nombre del item y el descuento resueltos. */
public interface DetalleItemCotizacionProjection {
    Long getIdItem();
    String getNombreItem();
    Double getCostoItem();
    Double getCantidadItemCotizacion();
    String getTipoDescuento();
    Double getValorDescuento();
    String getMotivoDescuento();
    /** Lo calcula cana.fn_descuento_linea, no el servicio. */
    Double getMontoDescuento();
    Double getSubtotalNeto();
}
