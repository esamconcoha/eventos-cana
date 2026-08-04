package com.canabackend.cana.projections;

public interface ReporteLineaCotizacionProjection {
    String getTipo();
    String getDescripcion();
    String getEspecificaciones();
    Double getCantidad();
    Double getPrecioUnitario();
    Double getSubtotal();
}
