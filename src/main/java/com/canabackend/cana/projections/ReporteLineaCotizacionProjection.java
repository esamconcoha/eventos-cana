package com.canabackend.cana.projections;

public interface ReporteLineaCotizacionProjection {
    String getTipo();
    String getDescripcion();
    String getEspecificaciones();
    Double getCantidad();
    Double getPrecioUnitario();
    /** Subtotal ANTES del descuento (cantidad x precio unitario). */
    Double getSubtotal();
    Double getMontoDescuento();
    /** Texto corto del descuento para el PDF ("-20%", "Exonerado", ...). */
    String getEtiquetaDescuento();
    /** Subtotal ya con el descuento aplicado. */
    Double getSubtotalNeto();
}
