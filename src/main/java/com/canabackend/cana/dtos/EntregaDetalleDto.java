package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle completo de una entrega. Trae los viajes ya expandidos con sus
 * items, y el estado item por item (pedido / enviado / pendiente), para que
 * la pantalla de detalle no necesite llamadas adicionales.
 */
@Data
public class EntregaDetalleDto {
    Long idEntrega;
    String correlativoPedido;
    String nombreClientePedido;
    String direccionPedido;
    Integer cantidadViajesAproximados;
    Integer cantidadViajesReales;
    Boolean pedidoFinalizado;
    LocalDateTime fechaInicioEntrega;
    LocalDateTime fechaFinEntrega;
    LocalDateTime fechaEvento;
    /** Fecha de entrega pactada con el cliente (planificada, no la ejecutada). */
    LocalDate fechaEntrega;
    Long salonEntrega;
    EstadoActualDto estadoActualPedido;
    /** Items del pedido con su avance de despacho. */
    List<ItemEntregaDto> items;

    /**
     * Servicios contratados (montaje, decoracion...). Van aca porque los
     * confirma la misma cuadrilla que registra los viajes, en el mismo
     * momento: obligarlos a ir a Pedidos para marcarlo seria un rodeo.
     * Lista vacia si el pedido solo alquilo mobiliario.
     */
    List<DetalleServicioPedidoListDto> servicios;
    List<ViajeEntregaDto> viajes;

    /** Si ya se subio la constancia firmada por el cliente. */
    Boolean constanciaFirmadaDisponible;
}
