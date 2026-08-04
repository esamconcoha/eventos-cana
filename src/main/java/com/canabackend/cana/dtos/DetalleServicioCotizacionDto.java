package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class DetalleServicioCotizacionDto {
    Long idServicio;
    double cantidad;
    double precioCotizado;
    String especificaciones;
}
