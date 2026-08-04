package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class DetalleServicioPedidoDto {
    Long idServicio;
    double cantidad;
    double precioAcordado;
    String especificaciones;
}
