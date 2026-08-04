package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActualizarPedidoDto {
    String direccionPedido;
    Long salonEntrega;
    LocalDate fechaEntrega;
    /** Fecha programada de recoleccion (pedidos_cana.fecha_recogido). */
    LocalDate fechaRecoleccion;
    LocalDateTime fechaEvento;
    List<DetallePedidoDto> detalles;
    List<DetalleServicioPedidoDto> detallesServicios;
}
