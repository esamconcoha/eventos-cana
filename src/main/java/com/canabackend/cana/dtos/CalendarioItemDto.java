package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una entrada de la agenda. El calendario devuelve una lista plana de estos y
 * el frontend los agrupa por {@link #fecha}; el discriminador es {@link #tipo}.
 *
 * Solo se publican los tipos que tienen un dato real detras:
 *  - EVENTO  <- pedidos_cana.fecha_evento (timestamp, con hora)
 *  - ENTREGA <- pedidos_cana.fecha_entrega (date, sin hora)
 *
 * Recoleccion y visita tecnica no existen todavia como dato: no hay tabla ni
 * columna que nadie escriba (fecha_recogido existe pero ningun flujo la llena).
 */
@Data
public class CalendarioItemDto {

    /** "EVENTO" o "ENTREGA". Ver CalendarioConstants. */
    String tipo;

    /** Dia en el que cae la entrada; es la clave de agrupacion de la grilla. */
    LocalDate fecha;

    /** Momento exacto cuando el origen lo tiene. Null en ENTREGA: fecha_entrega es DATE. */
    LocalDateTime fechaHora;

    String titulo;
    String correlativoPedido;
    String nombreCliente;
    String ubicacion;

    String codigoEstadoPedido;
    String nombreEstadoPedido;

    /** Solo ENTREGA: permite navegar directo al detalle de la entrega. */
    Long idEntrega;
    Boolean entregaFinalizada;
    Integer cantidadViajesReales;
    Integer cantidadViajesAproximados;
}
