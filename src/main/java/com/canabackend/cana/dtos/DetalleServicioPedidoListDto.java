package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DetalleServicioPedidoListDto {
    Long idServicio;
    String nombreServicio;
    double cantidad;
    double precioAcordado;
    String especificaciones;
    /** Momento en que se confirmo realizado. NULL = pendiente. */
    LocalDateTime fechaRealizado;
    /** Identifica la linea para poder marcarla realizada. */
    Long idDetalleServPedido;
}
