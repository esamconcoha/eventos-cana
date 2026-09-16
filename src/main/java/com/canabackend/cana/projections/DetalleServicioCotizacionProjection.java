package com.canabackend.cana.projections;

/** Linea de servicio de una cotizacion, con el nombre del servicio y el descuento resueltos. */
public interface DetalleServicioCotizacionProjection {
    Long getIdServicio();
    String getNombreServicio();
    Double getCantidad();
    Double getPrecioCotizado();
    String getEspecificaciones();
    String getTipoDescuento();
    Double getValorDescuento();
    String getMotivoDescuento();
    /** Lo calcula cana.fn_descuento_linea, no el servicio. */
    Double getMontoDescuento();
    Double getSubtotalNeto();
}
