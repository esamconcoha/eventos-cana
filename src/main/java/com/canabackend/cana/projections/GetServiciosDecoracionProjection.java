package com.canabackend.cana.projections;

public interface GetServiciosDecoracionProjection {
    Long getIdServicio();
    Long getIdCategoria();
    String getNombreCategoria();
    String getNombreServicio();
    String getDescripcionServicio();
    String getUnidadMedida();
    Boolean getRequiereDetalle();
    Boolean getEstadoServicio();
}
