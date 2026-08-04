package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class GetItemsExistentesDto {
    Long idItem;
    Integer idTipoItem;
    Double costoItem;
    String descripcionItem;
    Long cantidadItem;
    String observaciones;
    Integer cantidadFaltante;
    String nombreItem;
    Boolean estadoItem;
}
