package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetalleServicioCotizacionListDto {
    Long idServicio;
    String nombreServicio;
    double cantidad;
    double precioCotizado;
    String especificaciones;
}
