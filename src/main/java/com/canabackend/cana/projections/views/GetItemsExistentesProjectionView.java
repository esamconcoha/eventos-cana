package com.canabackend.cana.projections.views;

public interface GetItemsExistentesProjectionView {
    Long getIdItem();
    Integer getIdTipoItem();
    Double getCostoItem();
    String getDescripcionItem();
    Long getCantidadItem();
    String getObservaciones();
    Integer getCantidadFaltante();
    String getNombreItem();
    Boolean getEstadoItem();
}
