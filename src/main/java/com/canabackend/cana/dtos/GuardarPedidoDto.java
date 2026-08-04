package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GuardarPedidoDto {
    String nombreClientePedido;
    Integer telefonoClientePedido;
    String usuarioInternoPedido;
    String direccionPedido;
    LocalDateTime fechaEvento;
    Long salonEntrega;
    /** Obligatoria: abre la entrega junto con el pedido. */
    LocalDate fechaEntrega;
    /** Obligatoria, minimo 1: estimacion de viajes de la entrega. */
    Integer cantidadViajesAproximados;
    /**
     * Opcional: fecha programada de recoleccion (pedidos_cana.fecha_recogido).
     * Se puede dejar vacia y agendarla despues desde el modulo de Recolecciones.
     */
    LocalDate fechaRecoleccion;
    String codTipoEvento;
    List<DetallePedidoDto> detalles;
    List<DetalleServicioPedidoDto> detallesServicios;
}
