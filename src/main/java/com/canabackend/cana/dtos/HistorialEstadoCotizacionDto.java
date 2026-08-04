package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Un tramo del historial de estados de una cotizacion.
 * Misma forma que HistorialEstadoPedidoDto para que el modulo de trazabilidad
 * del front consuma las dos entidades con un solo componente.
 */
@Data
@AllArgsConstructor
public class HistorialEstadoCotizacionDto {
    Long idEstado;
    String codigoEstado;
    String nombreEstado;
    LocalDateTime fechaHoraInicio;
    /** null = tramo abierto: es el estado actual. */
    LocalDateTime fechaHoraFin;
}
