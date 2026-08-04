package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pedido elegible para abrirle una entrega: activo, con estado del ciclo de
 * vida anterior a la entrega y sin una fila en entregas_pedido.
 */
@Data
@AllArgsConstructor
public class PedidoDisponibleDto {
    String correlativoPedido;
    String nombreClientePedido;
    String direccionPedido;
    LocalDateTime fechaEvento;
    /** Fecha de entrega pactada. Puede venir null: los pedidos creados desde
     *  una cotizacion no la traen. No es criterio de elegibilidad. */
    LocalDate fechaEntrega;
    Long salonEntrega;
    String codigoEstadoPedido;
    String nombreEstadoPedido;
    /** Cantidad de lineas de item del pedido, para dimensionar la entrega. */
    Long totalItems;
}
