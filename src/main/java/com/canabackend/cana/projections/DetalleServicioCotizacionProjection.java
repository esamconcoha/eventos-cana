package com.canabackend.cana.projections;

/** Linea de servicio de una cotizacion, con el nombre del servicio resuelto. */
public interface DetalleServicioCotizacionProjection {
    Long getIdServicio();
    String getNombreServicio();
    Double getCantidad();
    Double getPrecioCotizado();
    String getEspecificaciones();
}
