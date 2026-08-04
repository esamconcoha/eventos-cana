package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EntregaListDto {
    Long idEntrega;
    String correlativoPedido;
    String nombreClientePedido;
    String direccionPedido;
    Integer cantidadViajesAproximados;
    Integer cantidadViajesReales;
    Boolean pedidoFinalizado;
    LocalDateTime fechaInicioEntrega;
    LocalDateTime fechaFinEntrega;
    /** Fecha/hora del evento del pedido. */
    LocalDateTime fechaEvento;
    /** Fecha de entrega pactada con el cliente (planificada, no la ejecutada). */
    LocalDate fechaEntrega;
    /** Misma forma que en EntregaDetalleDto: el estado viaja siempre como objeto. */
    EstadoActualDto estadoActualPedido;
}
