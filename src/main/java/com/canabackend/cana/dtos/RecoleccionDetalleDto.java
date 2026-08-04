package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecoleccionDetalleDto {
    Long idRecoleccion;
    String correlativoPedido;
    String nombreClientePedido;
    String direccionPedido;
    Integer cantidadViajesAproximados;
    Integer cantidadViajesReales;
    Boolean recoleccionFinalizada;
    LocalDateTime fechaInicioRecoleccion;
    LocalDateTime fechaFinRecoleccion;
    LocalDateTime fechaEvento;
    /** pedidos_cana.fecha_recogido: fecha PROGRAMADA de recoleccion. */
    LocalDate fechaRecoleccion;
    Long salonEntrega;
    EstadoActualDto estadoActualPedido;

    List<ItemRecoleccionDto> items;
    List<ViajeEntregaDto> viajes;

    /** Suma de lo que quedo sin volver. Mientras esta abierta es "por recolectar". */
    Double totalPendiente;
    /** Cuantos items distintos tienen pendiente > 0. */
    Integer itemsConPendiente;
}
