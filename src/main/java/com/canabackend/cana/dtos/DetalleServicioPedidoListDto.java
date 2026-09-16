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

    /** NIN | POR | MON | EXO. Ver DescuentoConstants. */
    String tipoDescuento;
    Double valorDescuento;
    String motivoDescuento;
    /** Ya resuelto en la BD: lo que descuenta esta linea, en Q. */
    Double montoDescuento;
    /** precioAcordado x cantidad - montoDescuento. Es la cifra que factura. */
    Double subtotalNeto;
}
