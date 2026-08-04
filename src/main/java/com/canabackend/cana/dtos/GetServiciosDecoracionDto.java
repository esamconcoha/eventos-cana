package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class GetServiciosDecoracionDto {
    Long idServicio;
    Long idCategoria;
    String nombreCategoria;
    String nombreServicio;
    String descripcionServicio;
    String unidadMedida;
    Boolean requiereDetalle;
    Boolean estadoServicio;
}
