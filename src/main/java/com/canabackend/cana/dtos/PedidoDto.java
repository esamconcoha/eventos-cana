package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoDto {
    String correlativoPedido;
    String nombreClientePedido;
    Integer telefonoClientePedido;
    String direccionPedido;
    LocalDateTime fechaEvento;
    LocalDate fechaEntrega;
    Long salonEntrega;
    LocalDate fechaConfirmacionPedido;
    /** pedidos_cana.fecha_recogido: fecha PROGRAMADA de recoleccion. */
    LocalDate fechaRecoleccion;
    Boolean pagado;
    String estadoPago;
    String usuarioInternoPedido;
    String codTipoEvento;
    String nombreTipoEvento;
    Integer idCotizacion;
    EstadoActualDto estadoActual;
    Double montoTotalPedido;
    Double saldoPendiente;
    List<DetallePedidoListDto> detalles;
    List<DetalleServicioPedidoListDto> detallesServicios;
}
